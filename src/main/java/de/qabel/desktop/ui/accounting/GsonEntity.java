package de.qabel.desktop.ui.accounting;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;

import java.util.LinkedList;
import java.util.List;

public abstract class GsonEntity {

	@Expose
	private String alias;
	@Expose
	private String email;
	@Expose
	private String phone;
	@Expose
	private Long created;
	@Expose
	private Long updated;
	@Expose
	private Long deleted;
	@Expose
	private List dropUrls = new LinkedList();


	public List<String> getDropUrls() {
		return dropUrls;
	}

	public void addDropUrl(String url) {
		dropUrls.add(url);
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Long getCreated() {
		return created;
	}

	public void setCreated(Long created) {
		this.created = created;
	}

	public Long getUpdated() {
		return updated;
	}

	public void setUpdated(Long updated) {
		this.updated = updated;
	}

	public Long getDeleted() {
		return deleted;
	}

	public void setDeleted(Long deleted) {
		this.deleted = deleted;
	}

	public void setDropUrls(List dropUrls) {
		this.dropUrls = dropUrls;
	}
}
