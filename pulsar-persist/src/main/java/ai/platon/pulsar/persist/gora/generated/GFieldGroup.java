/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package ai.platon.pulsar.persist.gora.generated;

import org.apache.gora.persistency.Persistent;

public class GFieldGroup extends org.apache.gora.persistency.impl.PersistentBase implements org.apache.avro.specific.SpecificRecord, org.apache.gora.persistency.Persistent {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"GFieldGroup\",\"namespace\":\"ai.platon.pulsar.persist.gora.generated\",\"fields\":[{\"name\":\"id\",\"type\":\"long\",\"default\":0},{\"name\":\"parentId\",\"type\":\"long\",\"default\":0},{\"name\":\"name\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"fields\",\"type\":{\"type\":\"map\",\"values\":[\"null\",\"string\"]},\"default\":{}}]}");
  private static final long serialVersionUID = -1092470099062642149L;
  /** Enum containing all data bean's fields. */
  public static enum Field {
    ID(0, "id"),
    PARENT_ID(1, "parentId"),
    NAME(2, "name"),
    FIELDS(3, "fields"),
    ;
    /**
     * Field's index.
     */
    private int index;

    /**
     * Field's name.
     */
    private String name;

    /**
     * Field's constructor
     * @param index field's index.
     * @param name field's name.
     */
    Field(int index, String name) {this.index=index;this.name=name;}

    /**
     * Gets field's index.
     * @return int field's index.
     */
    public int getIndex() {return index;}

    /**
     * Gets field's name.
     * @return String field's name.
     */
    public String getName() {return name;}

    /**
     * Gets field's attributes to string.
     * @return String field's attributes to string.
     */
    public String toString() {return name;}
  };

  public static final String[] _ALL_FIELDS = {
  "id",
  "parentId",
  "name",
  "fields",
  };

  /**
   * Gets the total field count.
   * @return int field count
   */
  public int getFieldsCount() {
    return GFieldGroup._ALL_FIELDS.length;
  }

    @Override
    public Persistent clone() {
        return null;
    }

    private long id;
  private long parentId;
  private java.lang.CharSequence name;
  private java.util.Map<java.lang.CharSequence,java.lang.CharSequence> fields;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return parentId;
    case 2: return name;
    case 3: return fields;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value) {
    switch (field$) {
    case 0: id = (java.lang.Long)(value); break;
    case 1: parentId = (java.lang.Long)(value); break;
    case 2: name = (java.lang.CharSequence)(value); break;
    case 3: fields = (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>)((value instanceof org.apache.gora.persistency.Dirtyable) ? value : new org.apache.gora.persistency.impl.DirtyMapWrapper((java.util.Map)value)); break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'id' field.
   */
  public java.lang.Long getId() {
    return id;
  }

  /**
   * Sets the value of the 'id' field.
   * @param value the value to set.
   */
  public void setId(java.lang.Long value) {
    this.id = value;
    setDirty(0);
  }
  
  /**
   * Checks the dirty status of the 'id' field. A field is dirty if it represents a change that has not yet been written to the database.
   * @param value the value to set.
   */
  public boolean isIdDirty() {
    return isDirty(0);
  }

  /**
   * Gets the value of the 'parentId' field.
   */
  public java.lang.Long getParentId() {
    return parentId;
  }

  /**
   * Sets the value of the 'parentId' field.
   * @param value the value to set.
   */
  public void setParentId(java.lang.Long value) {
    this.parentId = value;
    setDirty(1);
  }
  
  /**
   * Checks the dirty status of the 'parentId' field. A field is dirty if it represents a change that has not yet been written to the database.
   * @param value the value to set.
   */
  public boolean isParentIdDirty() {
    return isDirty(1);
  }

  /**
   * Gets the value of the 'name' field.
   */
  public java.lang.CharSequence getName() {
    return name;
  }

  /**
   * Sets the value of the 'name' field.
   * @param value the value to set.
   */
  public void setName(java.lang.CharSequence value) {
    this.name = value;
    setDirty(2);
  }
  
  /**
   * Checks the dirty status of the 'name' field. A field is dirty if it represents a change that has not yet been written to the database.
   * @param value the value to set.
   */
  public boolean isNameDirty() {
    return isDirty(2);
  }

  /**
   * Gets the value of the 'fields' field.
   */
  public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getFields() {
    return fields;
  }

  /**
   * Sets the value of the 'fields' field.
   * @param value the value to set.
   */
  public void setFields(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
    this.fields = (value instanceof org.apache.gora.persistency.Dirtyable) ? value : new org.apache.gora.persistency.impl.DirtyMapWrapper(value);
    setDirty(3);
  }
  
  /**
   * Checks the dirty status of the 'fields' field. A field is dirty if it represents a change that has not yet been written to the database.
   * @param value the value to set.
   */
  public boolean isFieldsDirty() {
    return isDirty(3);
  }

  /** Creates a new GFieldGroup RecordBuilder */
  public static ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder newBuilder() {
    return new ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder();
  }
  
  /** Creates a new GFieldGroup RecordBuilder by copying an existing Builder */
  public static ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder newBuilder(ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder other) {
    return new ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder(other);
  }
  
  /** Creates a new GFieldGroup RecordBuilder by copying an existing GFieldGroup instance */
  public static ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder newBuilder(ai.platon.pulsar.persist.gora.generated.GFieldGroup other) {
    return new ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder(other);
  }
  
  private static java.nio.ByteBuffer deepCopyToReadOnlyBuffer(
      java.nio.ByteBuffer input) {
    java.nio.ByteBuffer copy = java.nio.ByteBuffer.allocate(input.capacity());
    int position = input.position();
    input.reset();
    int mark = input.position();
    int limit = input.limit();
    input.rewind();
    input.limit(input.capacity());
    copy.put(input);
    input.rewind();
    copy.rewind();
    input.position(mark);
    input.mark();
    copy.position(mark);
    copy.mark();
    input.position(position);
    copy.position(position);
    input.limit(limit);
    copy.limit(limit);
    return copy.asReadOnlyBuffer();
  }
  
  /**
   * RecordBuilder for GFieldGroup instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<GFieldGroup>
    implements org.apache.avro.data.RecordBuilder<GFieldGroup> {

    private long id;
    private long parentId;
    private java.lang.CharSequence name;
    private java.util.Map<java.lang.CharSequence,java.lang.CharSequence> fields;

    /** Creates a new Builder */
    private Builder() {
      super(ai.platon.pulsar.persist.gora.generated.GFieldGroup.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing GFieldGroup instance */
    private Builder(ai.platon.pulsar.persist.gora.generated.GFieldGroup other) {
            super(ai.platon.pulsar.persist.gora.generated.GFieldGroup.SCHEMA$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = (java.lang.Long) data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.parentId)) {
        this.parentId = (java.lang.Long) data().deepCopy(fields()[1].schema(), other.parentId);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.name)) {
        this.name = (java.lang.CharSequence) data().deepCopy(fields()[2].schema(), other.name);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.fields)) {
        this.fields = (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>) data().deepCopy(fields()[3].schema(), other.fields);
        fieldSetFlags()[3] = true;
      }
    }

    /** Gets the value of the 'id' field */
    public java.lang.Long getId() {
      return id;
    }
    
    /** Sets the value of the 'id' field */
    public ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder setId(long value) {
      validate(fields()[0], value);
      this.id = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'id' field has been set */
    public boolean hasId() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'id' field */
    public ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder clearId() {
      fieldSetFlags()[0] = false;
      return this;
    }
    
    /** Gets the value of the 'parentId' field */
    public java.lang.Long getParentId() {
      return parentId;
    }
    
    /** Sets the value of the 'parentId' field */
    public ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder setParentId(long value) {
      validate(fields()[1], value);
      this.parentId = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'parentId' field has been set */
    public boolean hasParentId() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'parentId' field */
    public ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder clearParentId() {
      fieldSetFlags()[1] = false;
      return this;
    }
    
    /** Gets the value of the 'name' field */
    public java.lang.CharSequence getName() {
      return name;
    }
    
    /** Sets the value of the 'name' field */
    public ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder setName(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.name = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'name' field has been set */
    public boolean hasName() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'name' field */
    public ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder clearName() {
      name = null;
      fieldSetFlags()[2] = false;
      return this;
    }
    
    /** Gets the value of the 'fields' field */
    public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getFields() {
      return fields;
    }
    
    /** Sets the value of the 'fields' field */
    public ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder setFields(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
      validate(fields()[3], value);
      this.fields = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'fields' field has been set */
    public boolean hasFields() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'fields' field */
    public ai.platon.pulsar.persist.gora.generated.GFieldGroup.Builder clearFields() {
      fields = null;
      fieldSetFlags()[3] = false;
      return this;
    }
    
    @Override
    public GFieldGroup build() {
      try {
        GFieldGroup record = new GFieldGroup();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.Long) defaultValue(fields()[0]);
        record.parentId = fieldSetFlags()[1] ? this.parentId : (java.lang.Long) defaultValue(fields()[1]);
        record.name = fieldSetFlags()[2] ? this.name : (java.lang.CharSequence) defaultValue(fields()[2]);
        record.fields = fieldSetFlags()[3] ? this.fields : (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>) new org.apache.gora.persistency.impl.DirtyMapWrapper((java.util.Map)defaultValue(fields()[3]));
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
  
  public GFieldGroup.Tombstone getTombstone(){
  	return TOMBSTONE;
  }

  public GFieldGroup newInstance(){
    return newBuilder().build();
  }

  private static final Tombstone TOMBSTONE = new Tombstone();
  
  public static final class Tombstone extends GFieldGroup implements org.apache.gora.persistency.Tombstone {
  
      private Tombstone() { }
  
	  		  /**
	   * Gets the value of the 'id' field.
		   */
	  public java.lang.Long getId() {
	    throw new java.lang.UnsupportedOperationException("Get is not supported on tombstones");
	  }
	
	  /**
	   * Sets the value of the 'id' field.
		   * @param value the value to set.
	   */
	  public void setId(java.lang.Long value) {
	    throw new java.lang.UnsupportedOperationException("Set is not supported on tombstones");
	  }
	  
	  /**
	   * Checks the dirty status of the 'id' field. A field is dirty if it represents a change that has not yet been written to the database.
		   * @param value the value to set.
	   */
	  public boolean isIdDirty() {
	    throw new java.lang.UnsupportedOperationException("IsDirty is not supported on tombstones");
	  }
	
				  /**
	   * Gets the value of the 'parentId' field.
		   */
	  public java.lang.Long getParentId() {
	    throw new java.lang.UnsupportedOperationException("Get is not supported on tombstones");
	  }
	
	  /**
	   * Sets the value of the 'parentId' field.
		   * @param value the value to set.
	   */
	  public void setParentId(java.lang.Long value) {
	    throw new java.lang.UnsupportedOperationException("Set is not supported on tombstones");
	  }
	  
	  /**
	   * Checks the dirty status of the 'parentId' field. A field is dirty if it represents a change that has not yet been written to the database.
		   * @param value the value to set.
	   */
	  public boolean isParentIdDirty() {
	    throw new java.lang.UnsupportedOperationException("IsDirty is not supported on tombstones");
	  }
	
				  /**
	   * Gets the value of the 'name' field.
		   */
	  public java.lang.CharSequence getName() {
	    throw new java.lang.UnsupportedOperationException("Get is not supported on tombstones");
	  }
	
	  /**
	   * Sets the value of the 'name' field.
		   * @param value the value to set.
	   */
	  public void setName(java.lang.CharSequence value) {
	    throw new java.lang.UnsupportedOperationException("Set is not supported on tombstones");
	  }
	  
	  /**
	   * Checks the dirty status of the 'name' field. A field is dirty if it represents a change that has not yet been written to the database.
		   * @param value the value to set.
	   */
	  public boolean isNameDirty() {
	    throw new java.lang.UnsupportedOperationException("IsDirty is not supported on tombstones");
	  }
	
				  /**
	   * Gets the value of the 'fields' field.
		   */
	  public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getFields() {
	    throw new java.lang.UnsupportedOperationException("Get is not supported on tombstones");
	  }
	
	  /**
	   * Sets the value of the 'fields' field.
		   * @param value the value to set.
	   */
	  public void setFields(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
	    throw new java.lang.UnsupportedOperationException("Set is not supported on tombstones");
	  }
	  
	  /**
	   * Checks the dirty status of the 'fields' field. A field is dirty if it represents a change that has not yet been written to the database.
		   * @param value the value to set.
	   */
	  public boolean isFieldsDirty() {
	    throw new java.lang.UnsupportedOperationException("IsDirty is not supported on tombstones");
	  }
	
		  
  }

  private static final org.apache.avro.io.DatumWriter
            DATUM_WRITER$ = new org.apache.avro.specific.SpecificDatumWriter(SCHEMA$);
  private static final org.apache.avro.io.DatumReader
            DATUM_READER$ = new org.apache.avro.specific.SpecificDatumReader(SCHEMA$);

  /**
   * Writes AVRO data bean to output stream in the form of AVRO Binary encoding format. This will transform
   * AVRO data bean from its Java object form to it s serializable form.
   *
   * @param out java.io.ObjectOutput output stream to write data bean in serializable form
   */
  @Override
  public void writeExternal(java.io.ObjectOutput out)
          throws java.io.IOException {
    out.write(super.getDirtyBytes().array());
    DATUM_WRITER$.write(this, org.apache.avro.io.EncoderFactory.get()
            .directBinaryEncoder((java.io.OutputStream) out,
                    null));
  }

  /**
   * Reads AVRO data bean from input stream in it s AVRO Binary encoding format to Java object format.
   * This will transform AVRO data bean from it s serializable form to deserialized Java object form.
   *
   * @param in java.io.ObjectOutput input stream to read data bean in serializable form
   */
  @Override
  public void readExternal(java.io.ObjectInput in)
          throws java.io.IOException {
    byte[] __g__dirty = new byte[getFieldsCount()];
    in.read(__g__dirty);
    super.setDirtyBytes(java.nio.ByteBuffer.wrap(__g__dirty));
    DATUM_READER$.read(this, org.apache.avro.io.DecoderFactory.get()
            .directBinaryDecoder((java.io.InputStream) in,
                    null));
  }
  
}

