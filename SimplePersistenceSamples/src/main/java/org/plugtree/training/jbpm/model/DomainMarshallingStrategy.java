/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plugtree.training.jbpm.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.drools.marshalling.ObjectMarshallingStrategy;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;

/**
 *
 * @author esteban
 */
public class DomainMarshallingStrategy implements ObjectMarshallingStrategy {

    private Environment env;
    
    public DomainMarshallingStrategy(Environment env) {
        this.env = env;
    }
    
    @Override
    public boolean accept(Object object) {
        return object instanceof Document;
    }

    @Override
    public void write(ObjectOutputStream os, Object object) throws IOException {
        Document doc = (Document) object;
        
        os.writeUTF(object.getClass().getCanonicalName());
        os.writeObject(doc.getId());
        
        EntityManager em = this.getEntityManager();
        em.getTransaction().begin();
        em.merge(doc);
        em.getTransaction().commit();
        em.close();
    }

    @Override
    public Object read(ObjectInputStream is) throws IOException, ClassNotFoundException {
        String canonicalName = is.readUTF();
        Object id = is.readObject();
        
        EntityManager em = this.getEntityManager();
        Object result = em.find(Class.forName(canonicalName), id);
        em.close();
        
        return result;
    }
    
    public EntityManager getEntityManager(){
        EntityManagerFactory emf = (EntityManagerFactory) env.get("DOMAIN_EMF");
        return emf.createEntityManager();
    }

	@Override
	public Context createContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] marshal(Context arg0, ObjectOutputStream arg1, Object arg2)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object unmarshal(Context arg0, ObjectInputStream arg1, byte[] arg2,
			ClassLoader arg3) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
    
}
