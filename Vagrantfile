# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  ENV['VAGRANT_DEFAULT_PROVIDER'] = 'docker'

  config.vm.define "infra", primary: true do |infra|
    infra.vm.provider "docker" do |d|
      d.image = "qabel/infrastructure:latest"
      d.pull = true
      d.ports = [
        "5000:5000",
        "9696:9696",
        "9697:9697",
        "9698:9698",
      ]
    end

    infra.vm.network "forwarded_port", guest: 5000, host: 5000, auto_correct: true
    infra.vm.network "forwarded_port", guest: 9696, host: 9696, auto_correct: true
    infra.vm.network "forwarded_port", guest: 9697, host: 9697, auto_correct: true
    infra.vm.network "forwarded_port", guest: 9698, host: 9698, auto_correct: true
  end

  config.vm.define "installer", autostart: false do |installer|
    installer.vm.synced_folder ".", "/vagrant", mount_options: ["dmode=777","fmode=777"]
    installer.vm.provider "docker" do |d|
      d.build_dir = "installer"
      d.cmd = ["bash", "-c", "cd /vagrant/installer && bash prepare-setup.sh && bash build-setup.sh 0.0.0 dev"]
      d.remains_running = false
      d.name = "installer"
    end
  end
end
