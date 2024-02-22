package com.ibm.wala.classLoader;

import com.ibm.wala.core.util.strings.Atom;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import java.util.Collection;
import java.util.Collections;

/** dummy class representing a missing superclass */
public class PhantomClass extends SyntheticClass {

  /** @param T type reference describing this class */
  public PhantomClass(TypeReference T, IClassHierarchy cha) {
    super(T, cha);
  }

  @Override
  public boolean isPublic() {
    return false;
  }

  @Override
  public boolean isPrivate() {
    return false;
  }

  @Override
  public int getModifiers() throws UnsupportedOperationException {
    return 0;
  }

  @Override
  public IClass getSuperclass() {
    return getClassHierarchy().getRootClass();
  }

  @Override
  public Collection<? extends IClass> getDirectInterfaces() {
    return Collections.emptySet();
  }

  @Override
  public Collection<IClass> getAllImplementedInterfaces() {
    return Collections.emptySet();
  }

  @Override
  public IMethod getMethod(Selector selector) {
    return null;
  }

  @Override
  public IField getField(Atom name) {
    return null;
  }

  @Override
  public IMethod getClassInitializer() {
    return null;
  }

  @Override
  public Collection<? extends IMethod> getDeclaredMethods() {
    return Collections.emptySet();
  }

  @Override
  public Collection<IField> getAllInstanceFields() {
    return Collections.emptySet();
  }

  @Override
  public Collection<IField> getAllStaticFields() {
    return Collections.emptySet();
  }

  @Override
  public Collection<IField> getAllFields() {
    return Collections.emptySet();
  }

  @Override
  public Collection<? extends IMethod> getAllMethods() {
    return Collections.emptySet();
  }

  @Override
  public Collection<IField> getDeclaredInstanceFields() {
    return Collections.emptySet();
  }

  @Override
  public Collection<IField> getDeclaredStaticFields() {
    return Collections.emptySet();
  }

  @Override
  public boolean isReferenceType() {
    return true;
  }
}
