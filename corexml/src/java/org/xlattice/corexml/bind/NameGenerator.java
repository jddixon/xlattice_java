/* NameGenerator.java */
package org.xlattice.corexml.bind;

import java.util.HashMap;
import java.util.Map;

import static org.xlattice.util.StringLib.*;

/**
 * Generate standard method names from the base field name.
 */
public class NameGenerator {

    private static Map<String,String> adderNames   
                                        = new HashMap<String,String>();
    private static Map<String,String> collNames    
                                        = new HashMap<String,String>();
    private static Map<String,String> getterNames  
                                        = new HashMap<String,String>();
    private static Map<String,String> isNames  
                                        = new HashMap<String,String>();
    private static Map<String,String> removerNames 
                                        = new HashMap<String,String>();
    private static Map<String,String> setterNames  
                                        = new HashMap<String,String>();
    private static Map<String,String> sizerNames   
                                        = new HashMap<String,String>();
    private static Map<String,String> varNames     
                                        = new HashMap<String,String>();

    private NameGenerator() { }

    /**
     * If there are any letter-HYPHEN-letter sequences in a name,
     * replace them with letter-LETTER.  That is, the hyphen is 
     * removed and the following letter converted to upper case.
     */
    public static String dehyphenate(String s) {
        String sCopy = s.trim();
        int hyphenAt = sCopy.indexOf('-');
        if (hyphenAt == -1)
            return s;       // not the copy
        if (hyphenAt == 0)
            throw new IllegalArgumentException(
                    "cannot dehyphenate leading hyphen: " + s);
        if (hyphenAt == (s.length() - 1))
            throw new IllegalArgumentException(
                    "cannot dehyphenate trailing hyphen: " + s);
        if ( sCopy.indexOf("--") != -1)
            throw new IllegalArgumentException(
                    "cannot dehyphenate, multiple hyphens: " + s);
        
        StringBuffer sb = new StringBuffer (sCopy.substring(0, hyphenAt));
        for (int i = ++hyphenAt; i < s.length(); ) {
            sb.append( Character.toUpperCase( sCopy.charAt(i++) ) );
            if ( i == s.length() )
                break;
            hyphenAt = sCopy.indexOf('-', i);
            if ( hyphenAt == -1 ) {
                sb.append( sCopy.substring(i) );
                break;
            } else {
                sb.append( sCopy.substring(i, hyphenAt) );
                i = hyphenAt + 1;
            }
        }
        return sb.toString();
    }
    /**
     * Given a well-formed identifier that is NOT present in the
     * various name tables, populate them.
     *
     * XXX Although it is not enforced here, a well-formed identifier
     * has an alphabetic first character and zero or more additional
     * alphanumeric characters, where this is understood to include
     * the underscore character '_'.
     *
     * XXX LATER NOTE: we must allow tags containing hyphens and have
     * systematic rules for generating field names from them.
     *
     * @param lcName base form of key with first letter lower case
     */
    static void insertAllNames (String lcName) {
        if (lcName == null)
            throw new IllegalArgumentException("null base form of name");
        String ucName;
        if (lcName.equals("id"))
            ucName = "ID";
        else 
            ucName = ucFirst(lcName);
        adderNames.put      ( lcName, "add"     + ucName );
        getterNames.put     ( lcName, "get"     + ucName );
        isNames.put         ( lcName, "is"      + ucName );
        removerNames.put    ( lcName, "remove"  + ucName );
        setterNames.put     ( lcName, "set"     + ucName );
        String lcPlural;
        // XXX SIMPLISTIC ENGLISH-LANGUAGE PLURALIZATION RULES
        if ( lcName.endsWith("s") ) {
            lcPlural = lcName + "es";
        } else if ( lcName.endsWith("y") ) {
            if (lcName.endsWith("ey"))
                lcPlural = lcName + 's';
            else
                lcPlural = lcName.substring(0, lcName.length() - 1) + "ies";
        } else if ( lcName.endsWith("z") ) {
            lcPlural = lcName + "es";
        } else {
            lcPlural = lcName + 's';
        }
        String ucPlural;
        if (lcPlural.equals("ids"))
            ucPlural = "IDs";
        else
            ucPlural = ucFirst(lcPlural);
        sizerNames.put (lcName,   "size" + ucPlural);
        varNames.put   (lcName,   '_' + lcName);
        collNames.put  (lcName,   '_' + lcPlural);
    }
    /**
     * @return the name of the getter, "get" + ucFirst singular
     */
    public static String getterName (String base) {
        String lcBase = lcFirst(base.trim());
        if (lcBase.equals("iD"))
            lcBase = "id";
        String value = getterNames.get(lcBase);
        if (value == null) {
            insertAllNames(lcBase);
            value = getterNames.get(lcBase);
        }
        return value;
    }
    /**
     * @return the name of the boolean getter, "is" + ucFirst singular
     */
    public static String isName (String base) {
        String lcBase = lcFirst(base.trim());
        if (lcBase.equals("iD"))
            lcBase = "id";
        String value = isNames.get(lcBase);
        if (value == null) {
            insertAllNames(lcBase);
            value = isNames.get(lcBase);
        }
        return value;
    }
    /**
     * @return the name of the setter, "set" + ucFirst singular field name
     */
    public static String setterName (String base) {
        String lcBase = lcFirst(base.trim());
        if (lcBase.equals("iD"))
            lcBase = "id";
        String value = setterNames.get(lcBase);
        if (value == null) {
            insertAllNames(lcBase);
            value = setterNames.get(lcBase);
        }
        return value;
    }
    /**
     * @return the name of the adder, "add" + ucFirst singular
     */
    public static String adderName (String base) {
        String lcBase = lcFirst(base.trim());
        if (lcBase.equals("iD"))
            lcBase = "id";
        String value = adderNames.get(lcBase);
        if (value == null) {
            insertAllNames(lcBase);
            value = adderNames.get(lcBase);
        }
        return value;
    }
    /**
     * @return the name of the remover, "remove" + ucFirst singular
     */
    public static String removerName (String base) {
        String lcBase = lcFirst(base.trim());
        if (lcBase.equals("iD"))
            lcBase = "id";
        String value = removerNames.get(lcBase);
        if (value == null) {
            insertAllNames(lcBase);
            value = removerNames.get(lcBase);
        }
        return value;
    }
    /**
     * @return the name of the sizer, "size" + ucFirst plural
     */
    public static String sizerName (String base) {
        String lcBase = lcFirst(base.trim());
        if (lcBase.equals("iD"))
            lcBase = "id";
        String value = sizerNames.get(lcBase);
        if (value == null) {
            insertAllNames(lcBase);
            value = sizerNames.get(lcBase);
        }
        return value;
    }
    /**
     * @return the name of an instance variable, beginning with underscore
     */
    public static String varName (String base) {
        String lcBase = lcFirst(base.trim());
        if (lcBase.equals("iD"))
            lcBase = "id";
        String value = varNames.get(lcBase);
        if (value == null) {
            insertAllNames(lcBase);
            value = varNames.get(lcBase);
        }
        return value;
    }
    /**
     * @return the name of a collection, so the lower-case plural 
     */
    public static String collName (String base) {
        String lcBase = lcFirst(base.trim());
        if (lcBase.equals("iD"))
            lcBase = "id";
        String value = collNames.get(lcBase);
        if (value == null) {
            insertAllNames(lcBase);
            value = collNames.get(lcBase);
        }
        return value;
    }
}
