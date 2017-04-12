package com.google.classyshark.silverghost.translator.apk.dashboard.asciitable;

import java.util.function.Function;

/** Represents a column's title and alignment. */
public class Column {
	public final String header;
	public final Align headerAlign;
	public final Align dataAlign;

	/** A Column with a name. */
	public Column(String headerName) {
		this(headerName, Align.HEADER_DEFAULT, Align.DATA_DEFAULT);
	}
	
	/** A Column with a name and alignment. */
	public Column(String header, Align headerAlign, Align dataAlign) {
		this.header = header;
		this.headerAlign = headerAlign;
		this.dataAlign = dataAlign;
	}
	
	/** An object which can extract data with which to populate its column. */
	public <T> Data<T> with(Function<T, String> getter) {
		return new Data<T>(this, getter);
	}
	
	/** Represents a Data-driven column. */
	public static class Data<T> {
		public final Column column;
		public final Function<T, String> getter;

		private Data(Column column, Function<T, String> getter) {
			this.column = column;
			this.getter = getter;
		}
	}
}
