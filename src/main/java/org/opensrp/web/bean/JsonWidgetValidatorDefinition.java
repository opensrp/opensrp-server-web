package org.opensrp.web.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class JsonWidgetValidatorDefinition {

	@JsonProperty(value = "cannot_remove")
	private WidgetCannotRemove cannotRemove;

	@Getter
	@Setter
	@NoArgsConstructor
	@ToString
	public static class WidgetCannotRemove {

		private String title;

		private ArrayList<String> fields;
	}
}
