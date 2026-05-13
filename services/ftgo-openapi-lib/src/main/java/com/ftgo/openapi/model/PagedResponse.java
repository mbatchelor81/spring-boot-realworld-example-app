package com.ftgo.openapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Standardized paginated response wrapper.
 *
 * @param <T> the type of items in the page
 */
@Schema(description = "Paginated response wrapper")
public class PagedResponse<T> {

  @Schema(description = "Page content")
  private final List<T> content;

  @Schema(description = "Pagination metadata")
  private final PageMetadata page;

  public PagedResponse(List<T> content, PageMetadata page) {
    this.content = content;
    this.page = page;
  }

  public static <T> PagedResponse<T> of(
      List<T> content, int pageNumber, int pageSize, long totalElements) {
    int totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
    PageMetadata meta = new PageMetadata(pageNumber, pageSize, totalElements, totalPages);
    return new PagedResponse<>(content, meta);
  }

  public List<T> getContent() {
    return content;
  }

  public PageMetadata getPage() {
    return page;
  }

  /** Metadata describing the current page within a paginated result set. */
  @Schema(description = "Pagination metadata")
  public static class PageMetadata {

    @Schema(description = "Current page number (0-based)", example = "0")
    private final int number;

    @Schema(description = "Page size", example = "20")
    private final int size;

    @Schema(description = "Total number of elements", example = "42")
    private final long totalElements;

    @Schema(description = "Total number of pages", example = "3")
    private final int totalPages;

    public PageMetadata(int number, int size, long totalElements, int totalPages) {
      this.number = number;
      this.size = size;
      this.totalElements = totalElements;
      this.totalPages = totalPages;
    }

    public int getNumber() {
      return number;
    }

    public int getSize() {
      return size;
    }

    public long getTotalElements() {
      return totalElements;
    }

    public int getTotalPages() {
      return totalPages;
    }
  }
}
