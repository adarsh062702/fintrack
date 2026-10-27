
package com.adarsh.fintrack.dto;

public record CategoryResponse(
        Long id,
        String name,
        String type,
        boolean active
) {
}
