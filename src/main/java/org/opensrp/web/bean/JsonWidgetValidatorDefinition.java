package org.opensrp.web.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class JsonWidgetValidatorDefinition {

	@JsonProperty(value = "cannot_remove")
	private WidgetCannotRemove cannotRemove;

	public WidgetCannotRemove getCannotRemove() {
		return cannotRemove;
	}

	public void setCannotRemove(WidgetCannotRemove cannotRemove) {
		this.cannotRemove = cannotRemove;
	}

	public static class WidgetCannotRemove {
		private String title;
		private ArrayList<String> fields;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public ArrayList<String> getFields() {
			return fields;
		}

		public void setFields(ArrayList<String> fields) {
			this.fields = fields;
		}
	}
}
