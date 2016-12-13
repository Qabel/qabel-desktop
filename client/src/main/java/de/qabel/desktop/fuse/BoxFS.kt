package de.qabel.desktop.fuse

import de.qabel.box.storage.BoxNavigation
import de.qabel.box.storage.BoxVolume
import de.qabel.box.storage.dto.BoxPath
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.core.accounting.BoxClient
import jnr.ffi.Pointer
import ru.serce.jnrfuse.ErrorCodes
import ru.serce.jnrfuse.FuseFillDir
import ru.serce.jnrfuse.FuseStubFS
import ru.serce.jnrfuse.struct.FileStat
import ru.serce.jnrfuse.struct.FuseFileInfo
import ru.serce.jnrfuse.struct.Statvfs
import ru.serce.jnrfuse.struct.Timespec
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile

class BoxFS(val volume: BoxVolume, val boxClient: BoxClient) : FuseStubFS() {
    val root = volume.navigate()
    val tmp = createTempDir("BoxFsTmp")

    override fun create(path: String, mode: Long, fi: FuseFileInfo?): Int {
        val boxPath = path.toBoxPath()
        val nav: BoxNavigation = navigateToParent(boxPath)

        if (nav.hasFile(boxPath.name)) {
            return -ErrorCodes.EEXIST()
        }
        nav.upload(boxPath.name, "".toByteArray().inputStream(), 0L)
        return 0
    }

    override fun readdir(path: String, buf: Pointer, filter: FuseFillDir, offset: Long, fi: FuseFileInfo?): Int {
        val boxPath = path.toBoxPath()
        val nav = try { navigateTo(boxPath) } catch (e: QblStorageNotFound) { return -ErrorCodes.ENOENT() }

        filter.apply {

            apply(buf, ".", null, 0)
            apply(buf, "..", null, 0)
            nav.listFolders().forEach {
                apply(buf, it.name, null, 0)
            }
            nav.listFiles().forEach {
                apply(buf, it.name, null, 0)
            }
        }

        return 0
    }

    override fun unlink(path: String): Int {
        val boxPath = path.toBoxPath()
        val parent = navigateToParent(boxPath)
        if (parent.hasFile(boxPath.name)) {
            parent.delete(parent.getFile(boxPath.name))
        } else if (parent.hasFolder(boxPath.name)) {
            parent.delete(parent.getFolder(boxPath.name))
        } else {
            return -ErrorCodes.ENOENT()
        }
        return 0
    }

    override fun statfs(path: String?, stbuf: Statvfs): Int {
        val quotaState = boxClient.quotaState
        stbuf.f_bavail.set(quotaState.quota)
        stbuf.f_bfree.set(quotaState.quota - quotaState.size)
        stbuf.f_bsize.set(1)
        return 0
    }

    override fun rmdir(path: String): Int {
        val boxPath = path.toBoxPath()
        navigateToParent(boxPath).apply {
            delete(getFolder(boxPath.name))
        }
        return 0
    }

    override fun rename(oldpath: String, newpath: String): Int {
        val boxPath = oldpath.toBoxPath()
        val newBoxPath = newpath.toBoxPath()
        val parent = navigateToParent(boxPath)
        val localFile = resolveLocalTmpFile(boxPath, parent)

        if (parent.hasFile(boxPath.name)) {
            val file = parent.getFile(boxPath.name)
            if (!localFile.exists()) {  // either prefer local files or upload on fsync
                localFile.outputStream().use { outs ->
                    parent.download(file).use { ins ->
                        ins.copyTo(outs)
                    }
                }
            }
            if (parent.hasFile(newBoxPath.name)) {
                parent.overwrite(newBoxPath.name, localFile)
            } else {
                parent.upload(newBoxPath.name, localFile)
            }
            parent.delete(file)
        } else {
            return -ErrorCodes.ENOENT()
        }
        return 0
    }

    override fun truncate(path: String?, size: Long): Int {
        return 0
    }

    override fun utimens(path: String?, timespec: Array<out Timespec>?): Int {
        return 0
    }

    override fun chown(path: String?, uid: Long, gid: Long): Int {
        return 0
    }

    override fun getattr(path: String, stat: FileStat): Int {
        val boxPath = path.toBoxPath()
        if (boxPath == BoxPath.Root) {
            stat.st_mode.set(FileStat.S_IFDIR or 511)
        } else {
            val nav = navigateToParent(boxPath)

            if (nav.hasFolder(boxPath.name)) {
                stat.st_mode.set(FileStat.S_IFDIR or 511)
            } else if (nav.hasFile(boxPath.name)) {
                val file = nav.getFile(boxPath.name)
                stat.st_mode.set(FileStat.S_IFREG or 511)
                stat.st_size.set(nav.getFile(boxPath.name).size)
                stat.st_mtim.tv_sec.set(file.mtime / 1000L)
                stat.st_ctim.tv_sec.set(file.mtime / 1000L)
            } else {
                return -ErrorCodes.ENOENT()
            }
        }
        return 0
    }

    override fun open(path: String, fi: FuseFileInfo?): Int {
        val boxPath = path.toBoxPath()
        val parent = navigateToParent(boxPath)

        if (!parent.hasFile(boxPath.name)) {
            return -ErrorCodes.ENOENT()
        }
        if (parent.hasFolder(boxPath.name)) {
            return -ErrorCodes.EISDIR()
        }
        val file = parent.getFile(boxPath.name)

        val localPath = resolveLocalTmpFile(boxPath, parent)

        if (!localPath.exists()) {
            FileOutputStream(localPath).use { parent.download(file).copyTo(it) }
            localPath.deleteOnExit()
        }
        return 0
    }

    override fun mkdir(path: String, mode: Long): Int {
        val boxPath = path.toBoxPath()
        val parent = navigateToParent(boxPath)
        if (parent.hasFile(boxPath.name)) {
            return -ErrorCodes.EEXIST()
        }
        parent.createFolder(boxPath.name)

        return 0
    }

    override fun flush(path: String, fi: FuseFileInfo?): Int {
        navigateToParent(path.toBoxPath()).commit()
        return 0
    }

    override fun release(path: String, fi: FuseFileInfo?): Int {
        println("releasing $path")
        val boxPath = path.toBoxPath()
        val parent = navigateToParent(boxPath)

        if (!parent.hasFile(boxPath.name)) {
            return -ErrorCodes.ENOENT()
        }
        if (parent.hasFolder(boxPath.name)) {
            return -ErrorCodes.EISDIR()
        }
        val file = parent.getFile(boxPath.name)

        val localPath = resolveLocalTmpFile(boxPath, parent)

        if (localPath.exists()) {
            FileInputStream(localPath).use { parent.upload(file.name, it, localPath.length()) }
        }
        localPath.delete()
        return 0
    }

    private fun resolveLocalTmpFile(boxPath: BoxPath, parent: BoxNavigation): File {
        var localPath = tmp
        parent.path.toList().forEach { localPath = localPath.resolveSibling(it) }
        localPath.mkdirs()
        localPath = localPath.resolve(boxPath.name)
        return localPath
    }

    override fun read(path: String, buf: Pointer, size: Long, offset: Long, fi: FuseFileInfo?): Int {
        val boxPath = path.toBoxPath()
        val parent = navigateToParent(boxPath)
        if (!parent.hasFile(boxPath.name)) {
            return -ErrorCodes.ENOENT()
        }

        val length = parent.getFile(boxPath.name).size
        if (offset >= length) {
            return 0
        }
        val bytesToRead: Int = Math.min(length - offset, size).toInt()
        parent.download(boxPath.name).use { stream ->
            stream.skip(offset)
            buf.put(0, stream.readBytes(bytesToRead), 0, bytesToRead)
        }
        return bytesToRead
    }

    override fun write(path: String, buf: Pointer, size: Long, offset: Long, fi: FuseFileInfo?): Int {
        val boxPath = path.toBoxPath()
        val parent = navigateToParent(boxPath)

        if (!parent.hasFile(boxPath.name)) {
            return -ErrorCodes.ENOENT()
        }
        if (parent.hasFolder(boxPath.name)) {
            return -ErrorCodes.EISDIR()
        }

        val file = parent.getFile(boxPath.name)
        val localFile = resolveLocalTmpFile(boxPath, parent)

        val bytesToWrite: ByteArray = ByteArray(size.toInt())
        try {
            synchronized(this) {
                RandomAccessFile(localFile, "rw").use { file ->
                    file.seek(offset)
                    buf.get(0, bytesToWrite, 0, size.toInt())
                    file.write(bytesToWrite, 0, size.toInt())
                }
            }
        } catch (e: Exception) {
            println(e.message)
        }
        return size.toInt()
    }

    override fun getxattr(path: String, name: String, value: Pointer, size: Long): Int {
        println("$path: $name=$value ($size)")
            return super.getxattr(path, name, value, size)
    }

    override fun setxattr(path: String?, name: String?, value: Pointer?, size: Long, flags: Int): Int {
        println("$path: $name -> $value ? ($size) +$flags")
        return super.setxattr(path, name, value, size, flags)
    }

    override fun fsync(path: String?, isdatasync: Int, fi: FuseFileInfo?): Int {
        println("fsync $path isdatasync=$isdatasync")
        return super.fsync(path, isdatasync, fi)
    }

    private fun navigateToParent(boxPath: BoxPath): BoxNavigation {
        var nav: BoxNavigation = root
        if (boxPath.parent == BoxPath.Root) {
            return root
        }
        boxPath.toList().let {
            it.subList(0, it.size - 1)
        }.forEach {
            nav = nav.navigate(it)
        }
        return nav
    }

    private fun navigateTo(boxPath: BoxPath): BoxNavigation {
        var nav: BoxNavigation = root
        boxPath.toList().forEach { nav = nav.navigate(it) }
        return nav
    }
}

fun String.toBoxPath(): BoxPath {
    if (this == "/") {
        return BoxPath.Root
    }
    val parts = trim('/').replace("\\", "/").split("/")

    var path: BoxPath.FolderLike = BoxPath.Root
    parts.forEach { path = path.resolveFolder(it) }
    return path
}
