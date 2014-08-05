/* MockInterpreter.java */
package org.xlattice.projmgr.jml;

import java.io.IOException;
import java.io.Writer;

/**
 * The default interpreter recognizes all interface methods, consumes
 * their inputs, and outputs strings that identify the tokens seen.
 *
 * @author Jim Dixon
 */
public class MockInterpreter extends JmlWriter {

    // CONSTRUCTOR //////////////////////////////////////////////////
    /** 
     * @param w typically a StringWriter
     */
    public MockInterpreter(Writer writer) {
        super(writer);
    }
    private void wrap(String name, String text) {
        print(new StringBuffer("[").append(name).append(":'")
                                .append(text).append("']").toString());
    }
    private void wrap (String name, int n) {
        print(new StringBuffer("[").append(name).append(":")
                                .append(n).append("]").toString());
    }
    private void wrap(String name, String text, int n) {
        print(new StringBuffer("[").append(name).append(":'")
            .append(text).append("':").append(n).append("]").toString());
    }
    // DOCUMENT ///////////////////////////////////////////
    public void author (String name, String email) {
        print ("[author:'" + name + "':'" + email + "']");
    }
    public void begin() { 
        print("[begin]");
    }
    public void end() {
        print("[end]");
    }
    public void title  (String s) {
        wrap("title", s);
    }
    // LINE BREAKS ////////////////////////////////////////
    public void blankline() {
        print("[blankline]");
    }
    public void linebreak() {
        print("[linebreak]");
    }
    public void paragraph() {
        print("[para]");
    }
    // SIMPLE TEXT OUTPUT /////////////////////////////////
    public void text      (String s)         {
        wrap("text", s);
    }
    public void rawOutput  (String s)         {
        wrap("rawOutput", s);
    }
    // TEXT REFORMATTING //////////////////////////////////
    public void blockquote (String s) {
        wrap("blockquote", s);
    }
    public void bold   (String s) {
        wrap("bold", s);
    }
    public void code   (String s) {
        wrap("code", s);
    }
    public void italic (String s) {
        wrap("italic", s);
    }
    public void tt     (String s) {
        wrap("tt", s);
    }
    // LINKS //////////////////////////
    public void link   (String url) {
        wrap("link", url);
    }
    public void link   (String url, String tag) {
        if (tag == null)
            link (url);
        else {
            // scanner does not trim tag
            print( new StringBuffer("[link:'")
                    .append(url).append(":").append(tag.trim()).append("']")
                    .toString());
        }
    }
    // LISTS ////////////////////////////////////////////////////////
    public void beginOL (int n) {
        wrap ("beginOL", n);
    }
    public void endOL (int n) { 
        wrap ("endOL", n);
    }
    public void beginUL (int n) {
        wrap ("beginUL", n);
    }
    public void endUL (int n) { 
        wrap ("endUL", n);
    }
    public void beginListItem(int n) {
        wrap ("beginListItem", n);
    }
    public void endListItem(int n) {
        wrap ("endListItem", n);
    }
    // SECTIONS ///////////////////////////////////////////
    public void beginSection (String title, int n) {
        wrap("beginSection", title, n);
    }
    public void endSection (int n) {
        wrap("endSection", n);
    }
    public void beginSectionList (int n) {
        wrap ("beginSectionList", n);
    }
    public void endSectionList (int n) {
        wrap("endSectionList", n);
    }
    // TABLES /////////////////////////////////////////////
    public void beginTable() {
        print("[beginTable]");
    }
    public void endTable() {
        print("[endTable]");
    }
    public void  col() {
        print("[col]");
    }
    public void  row() {
        print("[row]");
    }
}
