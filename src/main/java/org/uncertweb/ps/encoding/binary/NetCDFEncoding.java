package org.uncertweb.ps.encoding.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.ParseException;

import ucar.nc2.FileWriter;
import ucar.nc2.NetcdfFile;
import ucar.nc2.util.IO;

public class NetCDFEncoding extends AbstractBinaryEncoding {

	public boolean isSupportedType(Class<?> classOf) {
		return classOf.equals(NetcdfFile.class);
	}

	public <T> T parse(InputStream inputStream, Class<T> type) throws ParseException {
		try {
			// FIXME: opening in memory is a bad idea (could run out of memory easily)
			byte[] bytes = IO.readContentsToByteArray(inputStream);
			NetcdfFile file = NetcdfFile.openInMemory("file", bytes);
			return type.cast(file);
		}
		catch (IOException e) {
			throw new ParseException("Couldn't read NetCDF from stream.", e);
		}
	}

	public <T> void encode(T object, OutputStream outputStream) throws EncodeException {
		try {
			// cast
			NetcdfFile file = (NetcdfFile)object;

			// FIXME: need better use of temporary files
			String filename = "temp" + System.currentTimeMillis();
			FileWriter.writeToFile(file, filename);

			// now back to stream
			FileInputStream fis = new FileInputStream(filename);
			byte[] buffer = new byte[1024];
			int n;
			while ((n = fis.read(buffer)) != -1) {
				outputStream.write(buffer, 0, n);
			}

			// remove file
			new File(filename).delete();
		}
		catch (IOException e) {
			throw new EncodeException("Couldn't write NetCDF to stream.", e);
		}
	}

	public boolean isSupportedMimeType(String mimeType) {
		return mimeType.equals(getDefaultMimeType());
	}

	public String getDefaultMimeType() {
		return "application/x-netcdf";
	}

}
