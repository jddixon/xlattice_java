/* Bindery.java */

/**
 * @author Jim Dixon
 **/

package org.xlattice.util.cmdline;

import java.io.PrintStream;
import java.lang.reflect.Field;

/**
 * Facility for binding an array of command line arguments to a 
 * set of fields in an object.
 *
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class Bindery {

    private CmdLineSpec spec;
    private int curPos;
    private ArgCursor cursor;

    /** command line options are bound to fields in this object */
    private Object obj;

    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Create a bindery from a command line specification.
     * @param set the command line specification
     */
    public Bindery(CmdLineSpec set) {
        spec = set;
    }

    /** 
     * Create a bindery from a set of command line options.
     * @param opts array of command line options
     */
    public Bindery(CmdLineOpt[] opts) {
        this(new CmdLineSpec(opts));
    }

    private void bindOptions(Object o) {
        Class clazz = o.getClass();
        CmdLineOpt[] opts = spec.getOptionDescriptors();
        for (int i = 0; i < opts.length; i++) {
            (opts[i]).bindField(clazz);
        }
    }

    /**
     * Bind the command line arguments to fields in the target.
     * @param args   array of command line arguments in String form
     * @param target object whose fields are being set
     */
    public Object bind(String[] args, Object target) {

        cursor = new ArgCursor(args);
        obj = target;
        bindOptions(target);

        while (cursor.hasNext()) {
            char cc = cursor.peek().charAt(0); // pointer not advanced
            if (cc == '-') {
                String arg = cursor.next();
                if (arg.length() <= 1){         // no option
                    throw new CmdLineException("missing option name");
                }
//              String oName = arg.substring(1);
//              CmdLineOpt opt = spec.findOption(oName);
//              opt.setValue (this);
                spec.findOption(arg.substring(1)).setValue(this);
            } else {
                break;
            }
        }
        return obj;
    }

    /** 
     * Bind the command line arguments, given a set of option descriptors.
     * If any arguments are not matched, an index to the first unmatched
     * argument is returned.
     *
     * @param args    an array of String command linearguments
     * @param options an array of option descriptors
     * @return        the index of the first unmatched argument
     */
    public static int bind(String[] args,  CmdLineOpt[] options,
                                                        Object target) {
        Bindery inst = new Bindery(options);
        inst.bind(args, target);
        return inst.cursor.index();
    }
    /////////////////////////////////////////////////////////////////

    /** 
     * @return a cursor over the command line argument list
     */
    ArgCursor cursor() {
        return cursor;
    }

    /**
     * Set a field in the target object (typically an instance of the
     * class with main(String []args) in it).
     * 
     * @param field the object field being assigned to
     * @param value the value being assigned
     */
    void setField (Field field, Object value) {
        try {
            field.set(obj, value);
        } catch (IllegalAccessException ex) {
            throw new OptionDescriptorException("Field " + field.getName() +
                " is not accessible in object of class " +
                obj.getClass().getName());
        }
    }

    /** 
     * Construct a usage message from the command line specification.
     */
    public String usageMsg () {
        StringBuffer sb = new StringBuffer().append(obj.getClass().getName());

        return sb.toString();
    }
//  public void listParameters(int width, PrintStream print) {

//      // scan once to find maximum parameter abbreviation length
//      int count = 0;
//      int maxlen = 0;
//      CmdLineOpt def = null;
//      while ((def = m_parameterSet.indexDef(count)) != null) {
//          int length = def.optionForHelp().length();
//          if (maxlen < length) {
//              maxlen = length;
//          }
//          count++;
//      }

//      // initialize for handling text generation
//      StringBuffer line = new StringBuffer(width);
//      int lead = maxlen + 2;
//      char[] blanks = new char[lead];
//      for (int i = 0; i < lead; i++) {
//          blanks[i] = ' ';
//      }

//      // scan again to print text of definitions
//      for (int i = 0; i < count; i++) {

//          // set up lead parameter abbreviation for first line
//          line.setLength(0);
//          def = m_parameterSet.indexDef(i);
//          line.append(' ');
//          line.append(def.optionForHelp());
//          line.append(blanks, 0, lead-line.length());

//          // format description text in as many lines as needed
//          String text = def.getDescription();
//          while (line.length()+text.length() > width) {

//              // scan for first line break position (even if beyond limit)
//              int limit = width - line.length();
//              int mark = text.indexOf(' ');
//              if (mark >= 0) {

//                  // find break position closest to limit
//                  int split = mark;
//                  while (mark >= 0 && mark <= limit) {
//                      split = mark;
//                      mark = text.indexOf(' ', mark+1);
//                  }

//                  // split the description for printing line
//                  line.append(text.substring(0, split));
//                  print.println(line.toString());
//                  line.setLength(0);
//                  line.append(blanks);
//                  text = text.substring(split+1);

//              } else {
//                  break;
//              }
//          }

//          // print remainder of description in single line
//          line.append(text);
//          print.println(line.toString());
//      }
//  } // GEEP

}
