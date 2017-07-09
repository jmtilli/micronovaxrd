package fi.micronova.tkk.xray;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class ZipOneOutputStream extends OutputStream {
    private final ZipOutputStream os;
    public ZipOneOutputStream(OutputStream inner, String name)
        throws IOException
    {
        ZipEntry e = new ZipEntry(name);
        os = new ZipOutputStream(inner);
        os.putNextEntry(e);
    }
    public void write(int b) throws IOException
    {
        os.write(b);
    }
    public void write(byte[] b, int off, int len) throws IOException
    {
        os.write(b, off, len);
    }
    public void close() throws IOException
    {
        os.close();
    }
    public void flush() throws IOException
    {
        os.flush();
    }
    public static void main(String[] args) throws Throwable
    {
        OutputStream os = new ZipOneOutputStream(new FileOutputStream(args[0]), "file.dat");
        byte[] ar1 = {1,2,3};
        byte[] ar2 = {4,5,6};
        byte[] ar3 = {7,8,9};
        int val4 = 10;
        os.write(ar1);
        os.write(ar1);
        os.write(ar3);
        os.write(val4);
        os.close();
    }
};
