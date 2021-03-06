package me.landmesser.simplecsv;

public class IntegerConverter extends ToStringConverter<Integer> {
  @Override
  public Integer parse(String value) throws CSVConversionException {
    return value == null ? null : Integer.parseInt(value);
  }
}
