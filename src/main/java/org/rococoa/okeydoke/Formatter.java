package org.rococoa.okeydoke;

/**
 * Formats things of type T to things of type C (for comparison), that can be assertEquals'd,
 *
 * Note that as the format conversion is only applied one way, it does not have to
 * be reversible - so don't worry too much about escaping etc.
 *
 * @param <C> the type of the comparison.
 */
public interface Formatter<T, C> {

    public C formatted(T object);
    public T emptyThing();
    public void assertEquals(C expected, C actual);

}
