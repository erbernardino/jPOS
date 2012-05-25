/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpos.util;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Allow runtime binding of jPOS's components (ISOChannels, Logger, MUXes, etc)
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class NameRegistrar implements Loggeable {

    private static NameRegistrar instance = new NameRegistrar();
    private Map<String,Object> registrar;
    private static ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public static class NotFoundException extends Exception {

        private static final long serialVersionUID = 8744022794646381475L;

        public NotFoundException() {
            super();
        }

        public NotFoundException(String detail) {
            super(detail);
        }
    }

    private NameRegistrar() {
        super();
        registrar = new HashMap<String,Object>();
    }

    public static Map<String,Object> getMap() {
        return getInstance().registrar;
    }

    /**
     * @return singleton instance
     */
    public static NameRegistrar getInstance() {
        return instance;
    }

    /**
     * register object
     *
     * @param key - key with which the specified value is to be associated.
     * @param value - value to be associated with the specified key
     */
    public static void register(String key, Object value) {
        Map<String,Object> map = getMap();
        LOCK.writeLock().lock();
        try {
            map.put(key, value);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    /**
     * @param key key whose mapping is to be removed from registrar.
     */
    public static void unregister(String key) {
        Map<String,Object> map = getMap();
        LOCK.writeLock().lock();
        try {
            map.remove(key);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    /**
     * @param key key whose associated value is to be returned.
     * @throws NotFoundException if key not present in registrar
     */
    public static Object get(String key) throws NotFoundException {
        LOCK.readLock().lock();
        try {
            Object obj = getMap().get(key);
            if (obj == null) {
                throw new NotFoundException(key);
            }
            return obj;
        } finally {
            LOCK.readLock().unlock();
        }
    }

    /**
     * @param key key whose associated value is to be returned, null if not
     * present.
     */
    public static Object getIfExists(String key) {
        LOCK.readLock().lock();
        try {
            return getMap().get(key);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public void dump(PrintStream p, String indent) {
        dump(p, indent, false);
    }

    public void dump(PrintStream p, String indent, boolean detail) {
        String inner = indent + "  ";
        p.println(indent + "--- name-registrar ---");
        LOCK.readLock().lock();
        try {
            for (Map.Entry<String,Object> entry : registrar.entrySet()) {
                Object obj = entry.getValue();
                String key = entry.getKey();
                if (key == null) {
                    key = "null";
                }
                String objectClassName = (obj == null) ? "<NULL>" :  obj.getClass().getName(); 
		p.println(inner
                        + key.toString() + ": "
                        + objectClassName);
                if (detail && obj instanceof Loggeable) {
                    ((Loggeable) obj).dump(p, inner + "  ");
                }
            }
        } finally {
            LOCK.readLock().unlock();
        }
    }
}
