package de.qabel.desktop.ui.accounting;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;

/**
 * Created by jmt on 18.01.16.
 */
public abstract class GsonEntity {

	@Expose
	public String alias;
	@Expose
	public String email;
	@Expose
	public String phone;
	@Expose
	public Long created;
	@Expose
	public Long updated;
	@Expose
	public Long deleted;
	@Expose
	public JsonArray dropUrls = new JsonArray();


	public JsonArray getDropUrls() {
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

	public void setDropUrls(JsonArray dropUrls) {
		this.dropUrls = dropUrls;
	}
}
