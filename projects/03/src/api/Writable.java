/**
 * The Writable interface - any object that needs to be emitted by or consumed by a mapper or a reducer needs to implement Writable.
 * Essentially, this promises that any Writable is also serializable.
 * @author Tomer Borenstein
 */
package api;

import java.io.Serializable;

public interface Writable extends Serializable{

}
