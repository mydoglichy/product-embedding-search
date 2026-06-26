package com.example.productembedding.dto;

public record CsvImportResponse(
        int importedCount,
        String message
) {
}
