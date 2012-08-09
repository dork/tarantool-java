package org.tarantool.facade;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.tarantool.core.Const.UP;
import org.tarantool.core.Operation;
import org.tarantool.core.SingleQueryConnectionFactory;
import org.tarantool.core.Tuple;

public class TarantoolTemplate<T,P> {
	int space = 0;
	Mapping<T> mapping;
	SingleQueryConnectionFactory connectionFactory;

	public TarantoolTemplate() {
		super();
	}

	public TarantoolTemplate(int space, SingleQueryConnectionFactory connectionFactory, Mapping<T> mapping) {
		super();
		this.space = space;
		this.mapping = mapping;
		this.connectionFactory = connectionFactory;
	}

	public ContidionFirst find() {
		return new Search(0, new String[] { mapping.getPrimaryKeyName() });
	}

	public ContidionFirst find(int index, String... fields) {
		return new Search(index, fields);
	}

	public abstract class ContidionFirst {
		public  abstract Search condition(Object... values);
	}

	public class Search extends ContidionFirst {

		public Search(int index, String[] fields) {
			this.indexFields = fields;
			this.index = index;
			keys = new ArrayList<Object[]>();
		}

		List<Object[]> keys;
		int index = 0;
		String[] indexFields;
		int offset = 0;
		int limit = Integer.MAX_VALUE;

		public Search offset(int offset) {
			this.offset = offset;
			return this;
		}

		public Search limit(int limit) {
			this.limit = limit;
			return this;
		}

		@Override
		public Search condition(Object... values) {
			if (indexFields != null) {
				mapping.checkFields(indexFields, values);
			}
			keys.add(values);
			return this;
		}

		public List<T> list() {
			Tuple[] tuples = new Tuple[keys.size()];
			for (int i = 0; i < keys.size(); i++) {
				tuples[i] = mapping.getSupport().create(keys.get(i));
			}
			List<Tuple> response = connectionFactory.getSingleQueryConnection().find(index, index, offset, limit, tuples);
			List<T> result = new ArrayList<T>();
			for (Tuple tuple : response) {
				result.add(mapping.fromTuple(tuple));
			}
			return result;

		}

		public T one() {
			List<T> list = list();
			return list.isEmpty() ? null : list.get(0);
		}
	}

	public Insert save(T value) {
		return new Insert(value);
	}

	public class Insert {
		T value;

		public Insert(T value) {
			this.value = value;
		}

		public int insert() {
			return connectionFactory.getSingleQueryConnection().insert(space, mapping.toTuple(value));
		}

		public int replace() {
			return connectionFactory.getSingleQueryConnection().replace(space, mapping.toTuple(value));
		}

		public int insertOrReplace() {
			return connectionFactory.getSingleQueryConnection().insertOrReplace(space, mapping.toTuple(value));
		}

		public T insertOrReplaceAndGet() {
			return mapping.fromTuple(connectionFactory.getSingleQueryConnection().insertOrReplaceAndGet(space, mapping.toTuple(value)));
		}

		public T insertAndGet() {
			return mapping.fromTuple(connectionFactory.getSingleQueryConnection().insertAndGet(space, mapping.toTuple(value)));
		}

		public T replaceAndGet() {
			return mapping.fromTuple(connectionFactory.getSingleQueryConnection().replaceAndGet(space, mapping.toTuple(value)));
		}
	}

	public Delete delete(P id) {
		return new Delete(id);
	}

	public class Delete {
		P id;

		private Delete(P id) {
			super();
			this.id = id;
		}

		public int delete() {
			return connectionFactory.getSingleQueryConnection().delete(space, id(id));
		}

		public T deleteAndGet() {
			return mapping.fromTuple(connectionFactory.getSingleQueryConnection().deleteAndGet(space, id(id)));
		}
	}
	
	public abstract class OperationFirst {
		public abstract Update add(String name, Long value);

		public abstract  Update add(String name, Integer value);
		

		public abstract Update and(String name, Integer value);

		public abstract Update and(String name, Long value);

		public abstract Update or(String name, Integer value);

		public abstract Update or(String name, Long value);

		public abstract Update xor(String name, Integer value);

		public abstract Update xor(String name, Long value);

		public abstract Update delete(String name, Object value);

		public abstract Update insert(String name, Object value);

		public abstract Update splice(String name, Integer offset, Integer delete, byte[] insert); 

		public abstract  Update splice(String name, String value, Integer offset, Integer delete, String insert); 
	}

	public OperationFirst update(P id) {
		return new Update(id);
	}

	public class Update extends OperationFirst{
		P id;

		List<Operation> ops = new ArrayList<Operation>();

		public T updateAndGet() {
			return mapping.fromTuple(connectionFactory.getSingleQueryConnection().updateAndGet(space, id(id), ops));
		}

		public int update() {
			return connectionFactory.getSingleQueryConnection().update(space, id(id), ops);
		}

		private Update(P id) {
			super();
			this.id = id;
		}

		@Override
		public Update add(String name, Long value) {
			ops.add(new Operation(UP.ADD, mapping.getFieldNo(name), mapping.support.create(value)));
			return this;
		}
		@Override
		public Update add(String name, Integer value) {
			ops.add(new Operation(UP.ADD, mapping.getFieldNo(name), mapping.support.create(value)));
			return this;
		}
		@Override
		public Update and(String name, Integer value) {
			ops.add(new Operation(UP.AND, mapping.getFieldNo(name), mapping.support.create(value)));
			return this;
		}
		@Override
		public Update and(String name, Long value) {
			ops.add(new Operation(UP.AND, mapping.getFieldNo(name), mapping.support.create(value)));
			return this;
		}
		@Override
		public Update or(String name, Integer value) {
			ops.add(new Operation(UP.OR, mapping.getFieldNo(name), mapping.support.create(value)));
			return this;
		}
		@Override
		public Update or(String name, Long value) {
			ops.add(new Operation(UP.OR, mapping.getFieldNo(name), mapping.support.create(value)));
			return this;
		}
		@Override
		public Update xor(String name, Integer value) {
			ops.add(new Operation(UP.XOR, mapping.getFieldNo(name), mapping.support.create(value)));
			return this;
		}
		@Override
		public Update xor(String name, Long value) {
			ops.add(new Operation(UP.XOR, mapping.getFieldNo(name), mapping.support.create(value)));
			return this;
		}
		@Override
		public Update delete(String name, Object value) {
			ops.add(new Operation(UP.DELETE, mapping.getFieldNo(name), mapping.support.create(value)));
			return this;
		}

		public Update insert(String name, Object value) {
			ops.add(new Operation(UP.INSERT, mapping.getFieldNo(name), mapping.support.create(value)));
			return this;
		}
		@Override
		public Update splice(String name, Integer offset, Integer delete, byte[] insert) {
			ops.add(new Operation(UP.SPLICE, mapping.getFieldNo(name), mapping.support.create(offset, delete, insert)));
			return this;
		}
		@Override
		public Update splice(String name, String value, Integer offset, Integer delete, String insert) {
			Integer bOffset;
			try {
				bOffset = value.substring(0, offset).getBytes(mapping.support.encoding).length;
				Integer bLength = value.substring(offset, offset + delete).getBytes(mapping.support.encoding).length;
				ops.add(new Operation(UP.SPLICE, mapping.getFieldNo(name), mapping.support.create(bOffset, bLength, insert.getBytes(mapping.support.encoding))));
			} catch (UnsupportedEncodingException ignored) {
			}
			return this;
		}

	}

	public int getSpace() {
		return space;
	}

	public void setSpace(int space) {
		this.space = space;
	}

	public Mapping<T> getMapping() {
		return mapping;
	}

	public void setMapping(Mapping<T> mapping) {
		this.mapping = mapping;
	}

	public SingleQueryConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(SingleQueryConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	private Tuple id(P id) {
		return mapping.support.create(id);
	}

}
