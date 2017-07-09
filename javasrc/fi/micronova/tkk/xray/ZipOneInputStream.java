package fi.micronova.tkk.xray;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class ZipOneInputStream extends InputStream {
    private final ZipInputStream is;
    private boolean eof = false;
    public ZipOneInputStream(InputStream inner) throws IOException
    {
        ZipEntry e;
        is = new ZipInputStream(inner);
        do {
            e = is.getNextEntry();
        } while (e != null && e.isDirectory());
        if (e == null)
        {
            throw new IOException("ZIP file has no files");
        }
    }
    public int read() throws IOException
    {
        ZipEntry e;
        int result;
        if (eof)
        {
            return -1;
        }
        result = is.read();
        if (result == -1)
        {
            do {
                e = is.getNextEntry();
            } while (e != null && e.isDirectory());
            if (e != null)
            {
                throw new IOException("ZIP file has multiple files");
            }
            eof = true;
        }
        return result;
    }
    public int read(byte[] b, int off, int len) throws IOException
    {
        ZipEntry e;
        int result;
        if (eof)
        {
            return -1;
        }
        result = is.read(b, off, len);
        if (result < 0)
        {
            do {
                e = is.getNextEntry();
            } while (e != null && e.isDirectory());
            if (e != null)
            {
                throw new IOException("ZIP file has multiple files");
            }
            eof = true;
        }
        return result;
    }
    public void close() throws IOException
    {
        is.close();
    }
    public int available() throws IOException
    {
        if (eof)
        {
            return 0;
        }
        return is.available();
    }
    public static void main(String[] args) throws Throwable
    {
        InputStream is = new ZipOneInputStream(new FileInputStream(args[0]));
        byte[] ar = new byte[1024];
        long cnt = 0;
        for (;;)
        {
            int increment = is.read(ar);
            if (increment <= 0)
            {
                break;
            }
            cnt += increment;
        }
        System.out.println("read " + cnt + " bytes");
    }
};
