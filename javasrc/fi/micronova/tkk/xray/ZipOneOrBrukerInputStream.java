package fi.micronova.tkk.xray;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import fi.iki.jmtilli.javaxmlfrag.*;
import javax.xml.parsers.*;
import org.xml.sax.SAXException;

public class ZipOneOrBrukerInputStream extends InputStream {
    private final ZipInputStream is;
    private boolean eof = false;
    private boolean bruker = false;
    private ZipEntry entry;
    public ZipOneOrBrukerInputStream(InputStream inner) throws IOException
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
        if (e.getName().matches("^Experiment[0-9]+/[^/]*\\.xml$"))
        {
            bruker = true;
        }
        if (e.getName().matches("^experimentCollection\\.xml$"))
        {
            bruker = true;
        }
        entry = e;
        if (bruker)
        {
            while (!this.getName().equals("Experiment0/RawData0.xml"))
            {
                if (!this.nextEntry())
                {
                    throw new IOException("Can't find Experiment0/RawData0.xml");
                }
            }
        }
    }
    public boolean isBruker()
    {
        return bruker;
    }
    public String getName() throws IOException
    {
        if (entry == null)
        {
            throw new IOException("getName() with no entry");
        }
        return entry.getName();
    }
    private boolean nextEntry() throws IOException
    {
        ZipEntry e;
        if (!bruker)
        {
            throw new IOException("nextEntry for !bruker");
        }
        do {
            e = is.getNextEntry();
        } while (e != null && e.isDirectory());
        entry = e;
        return e != null;
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
        if (result == -1 && !bruker)
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
        if (result < 0 && !bruker)
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
        ZipOneOrBrukerInputStream is =
            new ZipOneOrBrukerInputStream(new FileInputStream(args[0]));
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
        System.out.println("read " + cnt + " bytes for " + is.getName());
    }
};
