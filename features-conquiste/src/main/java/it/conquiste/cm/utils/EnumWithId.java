package it.conquiste.cm.utils;

/**
 * Interface for enum that will be stored with a SelectPolicy.
 * Some helper methods accept this interface and automatically
 * instantiate the correct value.
 *
 * @author mnova
 *
 */
public interface EnumWithId
{
    /**
     * Return the id of the value.
     *
     * @return an integer value.
     */
    int getId();
}
