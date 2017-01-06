package edu.uark.models.api.enums;

import java.util.HashMap;
import java.util.Map;

public enum ProductApiRequestStatus {
	OK(0),
	INVALID_INPUT(1),
	UNKNOWN_ERROR(2),
	NOT_FOUND(3),
	LOOKUP_CODE_ALREADY_EXISTS(4);
	
	public int getValue() {
		return value;
	}

	public static ProductApiRequestStatus map(int key) {
		if (valueMap == null) {
			valueMap = new HashMap<Integer, ProductApiRequestStatus>();

			for (ProductApiRequestStatus status : ProductApiRequestStatus.values()) {
				valueMap.put(status.getValue(), status);
			}
		}
		
		return (valueMap.containsKey(key) ? valueMap.get(key) : ProductApiRequestStatus.UNKNOWN_ERROR);
	}
	
	private int value;

	private static Map<Integer, ProductApiRequestStatus> valueMap = null;

	private ProductApiRequestStatus(int value) {
		this.value = value;
	}
}
