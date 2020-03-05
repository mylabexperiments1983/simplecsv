package me.landmesser.simplecsv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Offers the possibility to read a CSV file from a {@link Reader} and
 * transfer the result in a stream of objects of type <code>T</code>
 * <p>
 * The class of type <code>T</code> is parsed for annotations to
 * customize the behaviour of the parser.
 * </p>
 *
 * @param <T> the type of objects that should be created out of the csv input.
 */
@SuppressWarnings("unchecked")
public class CSVReader<T> extends ClassParser<T> implements Closeable {

  private final CSVParser parser;

  public CSVReader(Reader reader, Class<T> type, CSVFormat format) throws CSVException, IOException {
    super(type);
    parser = new CSVParser(reader, format);
  }

  public Stream<T> read() throws CSVParseException {
    return StreamSupport.stream(parser.spliterator(), false)
      .map(this::readSingle);
  }

  @Override
  public void close() throws IOException {
    parser.close();
  }

  private T readSingle(CSVRecord record) throws CSVParseException {
    try {
      final T result = getType().getConstructor().newInstance();
      Iterator<String> iterator = record.iterator();
      getEntries().forEach(entry -> {
        try {
          if (iterator.hasNext()) {
            evaluate(result, iterator.next(), entry);
          }
        } catch (CSVParseException e) {
          e.printStackTrace();
        }
      });
      return result;
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new CSVParseException("Could not instantiate class " + getType(), e);
    } catch (NoSuchMethodException e) {
      throw new CSVParseException("Class " + getType() + " has no default constructor", e);
    }
  }

  private <R> void evaluate(T targetObject, String value, CSVEntry<R> entry) throws CSVParseException {
    try {
      R converted;
      Class<R> entryType = entry.getType();
      if (entry.getConverter() != null) {
        converted = entry.getConverter().parse(value);
      } else {
        converted = parse(entryType, value);
      }
      // TODO: workaround
      if (converted != null) {
        Method method = getType().getDeclaredMethod(determineSetter(entry), entry.getType());
        method.invoke(targetObject, converted);
      }
    } catch (NoSuchMethodException e) {
      Logger.getLogger(getClass().getSimpleName()).warning(("No setter found for " + entry.getFieldName()));
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new CSVParseException("Could not invoke getter for field " + entry.getFieldName(), e);
    }
  }

}
