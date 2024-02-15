package com.wanted.preonboarding.ticket.domain.dto;

public enum DiscountType {
  NONE(0),
  TELECOM(1),
  NEW(2);
  private final int category;
  DiscountType(int category) {
    this.category = category;
  }

  public int getCategory() {
    return category;
  }
}
