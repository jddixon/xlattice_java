/* Version.java */
package org.xlattice;

/**
 * This is a first step towards a standard approach to version 
 * management for all XLattice components.  What's wanted is 
 * automatic versioning, with the build number incremented by Ant
 * or Eclipse.
 *
 * There should be a subclass of Version in the main source 
 * directory of each XLattice component.  For example, CoreXml
 * has one as org.xlattice.corexml.Version.  The source files for 
 * these <emp>should</emp> be automatically generated but are
 * currently being manually updated.
 *
 * Together the major, minor, and optional decimal version number 
 * are construed as a single number, n.m[.d].  The build number 
 * is to be incremented on each build, with or without a change in 
 * version number.
 * 
 * XLattice Version numbers are compatible with JNLP version numbers.
 *
 * @author Jim Dixon
 */
public abstract class Version {
    /** the fully qualified package name in dotted form */
    private final String packageName;
    /** major version number */
    private final int major;
    /** minor version number */
    private final int minor;
    /** decimal version number */
    private final int decimal;
    /** build number */
    private final int build;

    private Version () {
        this("", 0, 0, 0, 0);
    }
    /**
     * @param pkg      package name in dotted form (a.b.c.d)
     * @param major    major part of version number
     * @param minor    minor part of version number
     * @param decimal  decimal part of version number
     * @param build    build number
     */
    public  Version (String pkg, int major, int minor, 
                     int decimal, int build) {
        if (pkg == null || pkg.length() == 0)
            throw new IllegalArgumentException("null or empty package name");
        packageName = pkg;
        this.major = major;
        this.minor = minor;
        this.decimal = decimal;
        this.build = build;

    }
    public  Version (String pkg, int major, int minor, int build) {
        this(pkg, major, minor, -1, build);
    }
    /** 
     * Returns the package name, version number, and the build 
     * number if it is non-zero.  The package name is in fully
     * qualified dotted form.  The version number is returned as
     * a decimal number, "major.minor".  If the decimal part is
     * non-negative, the version number is construed as a number
     * with three parts, M.m.d, "major.minor.decimal".
     *
     * If the build number is a positive integer, it is appended 
     * as "-NNNNN", where NNNNN represents the integer value, possibly
     * padded with leading zeroes.
     *
     * @return formatted version number
     */
    public final String getVersion () {
        StringBuffer sb = new StringBuffer(packageName)
            .append(" ").append(major).append(".").append(minor);
        if (decimal >= 0) 
            sb.append('.').append(decimal);
        if (build > 0){
            sb.append('-');
            // pad to 5 digits
            for (int limit = 10000; build < limit; limit /= 10)
                sb.append('0');
            sb.append(build);
        }
        return sb.toString();
    }
    /**
     * Returns the fully qualified package name in dotted form,
     * such as "org.xlattice.util".
     *
     * @return the fully qualified package name
     */
    public String getPackage () {
        return packageName;
    }
    /**
     * Returns the first part of the version number.  This is a
     * non-negative integer.  All releases with the same major version
     * number should be compatible, in the sense that releases with
     * a higher version number should be backward compatible with 
     * those with a lower version number.  
     * 
     * @return the major part of the version number
     */
    public int getMajor() {
        return major;
    }
    /**
     * Returns the second part of the version number.  This is a
     * non-negative integer.  If the major version number is the same,
     * then releases with higher minor version numbers should be
     * backward compatible with releases with lower version numbers.
     * This specifically means that if the major version number is the
     * same and N1 and N2 are minor release numbers, then if N2 is 
     * higher than N1, then all N1 unit tests should succeed with 
     * release N2 software.
     * 
     * @return the minor part of the version number
     */
    public int getMinor() {
        return minor;
    }
    /**
     * Returns the third part of the version number.  This is a
     * non-negative integer; if the decimal part is negative, it
     * should be ignored.  
     *
     * If the major and minor release numbers are the same, then 
     * decimal releases represent steps towards implementing the
     * target M.m functionality.  If d2 is greater than d1, then
     * M.m.d1 unit tests should succeed with M.m.d2, at least to
     * the degree that they succeeded with M.m.d1.
     */
    public int getDecimal () {
        return decimal;
    }
    /**
     * Returns the build number.  Build numbers increase with each 
     * release, and should increase by 1.  When the major release
     * number is incremented, the build number is also incremented.
     * That is, if version 1.2 build 5001 is followed by version 2.0,
     * the build number should be 5002, so the full version number
     * will be "2.0-05002".
     * 
     * @return the build part of the version number
     */
    public int getBuild () {
        return build;
    }
}
