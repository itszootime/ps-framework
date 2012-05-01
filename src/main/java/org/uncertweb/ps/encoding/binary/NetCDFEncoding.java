package org.uncertweb.ps.encoding.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.ParseException;

import ucar.nc2.FileWriter;
import ucar.nc2.NetcdfFile;
import ucar.nc2.util.IO;

public class NetCDFEncoding extends AbstractBinaryEncoding {

	public boolean isSupportedClass(Class<?> classOf) {
		return classOf.equals(NetcdfFile.class);
	}

	public Object parse(InputStream is, Class<?> classOf) throws ParseException {
		try {
			// TODO: opening in memory slow/unsafe (could run out of memory easily)
			byte[] bytes = IO.readContentsToByteArray(is);
			NetcdfFile file = NetcdfFile.openInMemory("file", bytes);
			return file;
		}
		catch (Exception e) {
			throw new ParseException("Couldn't parse NetCDF from stream.", e);
		}
	}

	public void encode(Object o, OutputStream os) throws EncodeException {
		try {
			// cast
			NetcdfFile file = (NetcdfFile) o;

			// TODO: doesn't seem like a nice way to write the file
			String filename = "temp" + System.currentTimeMillis();
			FileWriter.writeToFile(file, filename);
			
			// now back to stream
			FileInputStream fis = new FileInputStream(filename);
			byte[] buffer = new byte[1024];
			int n;
			while ((n = fis.read(buffer)) != -1) {
				os.write(buffer, 0, n);
			}
			
			// remove file
			new File(filename).delete();
		}
		catch (Exception e) {
			// no checked exceptions are thrown, but i'm suspicious
			throw new EncodeException("Couldn't generate NetCDF for object.", e);
		}
	}

	public boolean isSupportedMimeType(String mimeType) {
		return mimeType.equals("application/x-netcdf");
	}

}
