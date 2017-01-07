/*
 * Copyright 2016-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.jvm.java.abi.source;

import com.facebook.buck.util.exportedfiles.Nullable;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;

/**
 * An implementation of {@link Types} using just the AST of a single module, without its
 * dependencies. Of necessity, such an implementation will need to make assumptions about the
 * meanings of some names, and thus must be used with care. See documentation for individual
 * methods and {@link com.facebook.buck.jvm.java.abi.source} for more information.
 */
class TreeBackedTypes implements Types {
  @Override
  @Nullable
  public Element asElement(TypeMirror t) {
    if (t.getKind() == TypeKind.DECLARED) {
      return ((DeclaredType) t).asElement();
    } else if (t.getKind() == TypeKind.TYPEVAR) {
      throw new UnsupportedOperationException("Type variables not yet implemented");
    }

    return null;
  }

  @Override
  public boolean isSameType(TypeMirror t1, TypeMirror t2) {
    if (t1.getKind() != t2.getKind()) {
      return false;
    }

    switch (t1.getKind()) {
      case DECLARED:
        return isSameType((DeclaredType) t1, (DeclaredType) t2);
      case ARRAY:
        return isSameType(((ArrayType) t1).getComponentType(), ((ArrayType) t2).getComponentType());
      //$CASES-OMITTED$
      default:
        throw new UnsupportedOperationException(
            String.format("isSameType NYI for kind %s", t1.getKind()));
    }
  }

  private boolean isSameType(DeclaredType t1, DeclaredType t2) {
    if (!t1.asElement().equals(t2.asElement())) {
      return false;
    }

    List<? extends TypeMirror> args1 = t1.getTypeArguments();
    List<? extends TypeMirror> args2 = t2.getTypeArguments();
    if (args1.size() != args2.size()) {
      // TODO(jkeljo): This is impossible in code that will compile, but we will eventually need
      // to gracefully error out on code with some kinds of errors (such as providing the
      // wrong number of type arguments). Whenever we do that work, the body of this statement
      // should throw.
      return false;
    }

    for (int i = 0; i < args1.size(); i++) {
      if (!isSameType(args1.get(i), args2.get(i))) {
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(TypeMirror t1, TypeMirror t2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSubsignature(
      ExecutableType m1, ExecutableType m2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<? extends TypeMirror> directSupertypes(TypeMirror t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TypeMirror erasure(TypeMirror t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TypeElement boxedClass(PrimitiveType p) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PrimitiveType unboxedType(TypeMirror t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TypeMirror capture(TypeMirror t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PrimitiveType getPrimitiveType(TypeKind kind) {
    throw new UnsupportedOperationException();
  }

  @Override
  public NullType getNullType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public NoType getNoType(TypeKind kind) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ArrayType getArrayType(TypeMirror componentType) {
    return new StandaloneArrayType(componentType);
  }

  @Override
  public WildcardType getWildcardType(
      TypeMirror extendsBound, TypeMirror superBound) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs) {
    return new StandaloneDeclaredType(typeElem, Arrays.asList(typeArgs));
  }

  @Override
  public DeclaredType getDeclaredType(
      DeclaredType containing, TypeElement typeElem, TypeMirror... typeArgs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TypeMirror asMemberOf(DeclaredType containing, Element element) {
    throw new UnsupportedOperationException();
  }
}
