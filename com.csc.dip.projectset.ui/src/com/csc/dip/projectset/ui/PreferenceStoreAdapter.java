/*******************************************************************************
 * Copyright (C) 2018 DXC Technology
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.csc.dip.projectset.ui;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
/**
 * The class <code>PreferencesAdapter</code> adapts instances of the class
 * {@link <code>Preferences</code>} to implement the interface {@link
 * <code>IPreferenceStore</code>}.
 */
public class PreferenceStoreAdapter implements IPreferenceStore {
	/**
	 * The object that is being adapted to the <code>IPreferenceStore</code>
	 * interface.
	 *
	 * @see org.eclipse.core.runtime.Preferences
	 * @see org.eclipse.jface.preference.IPreferenceStore
	 */
	private Preferences prefs;

	/**
	 * Create a new adapter for the <code>Preferences</code> instance.
	 *
	 * @param prefs the object that is being adapted
	 *
	 * @see org.eclipse.core.runtime.Preferences
	 */
	public PreferenceStoreAdapter(Preferences prefs) {
		this.prefs = prefs;
	}

	/**
	 * Adds a property change listener to this preference store.
	 *
	 * @param listener a property change listener
	 * @see org.eclipse.jface.preference.IPreferenceStore#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		// ignored
	}

	/**
	 * Returns whether the named preference is known to this preference
	 * store.
	 *
	 * @param name the name of the preference
	 * @return <code>true</code> if either a current value or a default
	 * value is known for the named preference, and <code>false</code>otherwise
	 * @see org.eclipse.jface.preference.IPreferenceStore#contains(java.lang.String)
	 */
	public boolean contains(String name) {
		return prefs.contains(name);
	}

	/**
	 * Fires a property change event corresponding to a change to the
	 * current value of the preference with the given name.
	 * <p>
	 * This method is provided on this interface to simplify the implementation
	 * of decorators. There is normally no need to call this method since
	 * <code>setValue</code> and <code>setToDefault</code> report such
	 * events in due course. Implementations should funnel all preference
	 * changes through this method.
	 * </p>
	 *
	 * @param name the name of the preference, to be used as the property
	 * in the event object
	 * @param oldValue the old value
	 * @param newValue the new value
	 * @see org.eclipse.jface.preference.IPreferenceStore#firePropertyChangeEvent(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		// ignored
	}

	/**
	 * Returns the current value of the boolean-valued preference with the
	 * given name.
	 * Returns the default-default value (<code>false</code>) if there
	 * is no preference with the given name, or if the current value
	 * cannot be treated as a boolean.
	 *
	 * @param name the name of the preference
	 * @return the boolean-valued preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#getBoolean(java.lang.String)
	 */
	public boolean getBoolean(String name) {
		return prefs.getBoolean(name);
	}

	/**
	 * Returns the default value for the boolean-valued preference
	 * with the given name.
	 * Returns the default-default value (<code>false</code>) if there
	 * is no default preference with the given name, or if the default
	 * value cannot be treated as a boolean.
	 *
	 * @param name the name of the preference
	 * @return the default value of the named preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultBoolean(java.lang.String)
	 */
	public boolean getDefaultBoolean(String name) {
		return prefs.getDefaultBoolean(name);
	}

	/**
	 * Returns the default value for the double-valued preference
	 * with the given name.
	 * Returns the default-default value (<code>0.0</code>) if there
	 * is no default preference with the given name, or if the default
	 * value cannot be treated as a double.
	 *
	 * @param name the name of the preference
	 * @return the default value of the named preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultDouble(java.lang.String)
	 */
	public double getDefaultDouble(String name) {
		return prefs.getDefaultDouble(name);
	}

	/**
	 * Returns the default value for the float-valued preference
	 * with the given name.
	 * Returns the default-default value (<code>0.0f</code>) if there
	 * is no default preference with the given name, or if the default
	 * value cannot be treated as a float.
	 *
	 * @param name the name of the preference
	 * @return the default value of the named preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultFloat(java.lang.String)
	 */
	public float getDefaultFloat(String name) {
		return prefs.getDefaultFloat(name);
	}

	/**
	 * Returns the default value for the integer-valued preference
	 * with the given name.
	 * Returns the default-default value (<code>0</code>) if there
	 * is no default preference with the given name, or if the default
	 * value cannot be treated as an integer.
	 *
	 * @param name the name of the preference
	 * @return the default value of the named preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultInt(java.lang.String)
	 */
	public int getDefaultInt(String name) {
		return prefs.getDefaultInt(name);
	}

	/**
	 * Returns the default value for the long-valued preference
	 * with the given name.
	 * Returns the default-default value (<code>0L</code>) if there
	 * is no default preference with the given name, or if the default
	 * value cannot be treated as a long.
	 *
	 * @param name the name of the preference
	 * @return the default value of the named preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultLong(java.lang.String)
	 */
	public long getDefaultLong(String name) {
		return prefs.getDefaultLong(name);
	}

	/**
	 * Returns the default value for the string-valued preference
	 * with the given name.
	 * Returns the default-default value (the empty string <code>""</code>)
	 * is no default preference with the given name, or if the default
	 * value cannot be treated as a string.
	 *
	 * @param name the name of the preference
	 * @return the default value of the named preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultString(java.lang.String)
	 */
	public String getDefaultString(String name) {
		return prefs.getDefaultString(name);
	}

	/**
	 * Returns the current value of the double-valued preference with the
	 * given name.
	 * Returns the default-default value (<code>0.0</code>) if there
	 * is no preference with the given name, or if the current value
	 * cannot be treated as a double.
	 *
	 * @param name the name of the preference
	 * @return the double-valued preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDouble(java.lang.String)
	 */
	public double getDouble(String name) {
		return prefs.getDouble(name);
	}

	/**
	 * Returns the current value of the float-valued preference with the
	 * given name.
	 * Returns the default-default value (<code>0.0f</code>) if there
	 * is no preference with the given name, or if the current value
	 * cannot be treated as a float.
	 *
	 * @param name the name of the preference
	 * @return the float-valued preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#getFloat(java.lang.String)
	 */
	public float getFloat(String name) {
		return prefs.getFloat(name);
	}

	/**
	 * Returns the current value of the integer-valued preference with the
	 * given name.
	 * Returns the default-default value (<code>0</code>) if there
	 * is no preference with the given name, or if the current value
	 * cannot be treated as an integter.
	 *
	 * @param name the name of the preference
	 * @return the int-valued preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#getInt(java.lang.String)
	 */
	public int getInt(String name) {
		return prefs.getInt(name);
	}

	/**
	 * Returns the current value of the long-valued preference with the
	 * given name.
	 * Returns the default-default value (<code>0L</code>) if there
	 * is no preference with the given name, or if the current value
	 * cannot be treated as a long.
	 *
	 * @param name the name of the preference
	 * @return the long-valued preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#getLong(java.lang.String)
	 */
	public long getLong(String name) {
		return prefs.getLong(name);
	}

	/**
	 * Returns the current value of the string-valued preference with the
	 * given name.
	 * Returns the default-default value (the empty string <code>""</code>)
	 * if there is no preference with the given name, or if the current value
	 * cannot be treated as a string.
	 *
	 * @param name the name of the preference
	 * @return the string-valued preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#getString(java.lang.String)
	 */
	public String getString(String name) {
		return prefs.getString(name);
	}

	/**
	 * Returns whether the current value of the preference with the given name
	 * has the default value.
	 *
	 * @param name the name of the preference
	 * @return <code>true</code> if the preference has a known default value
	 * and its current value is the same, and <code>false</code> otherwise
	 * (including the case where the preference is unknown to this store)
	 * @see org.eclipse.jface.preference.IPreferenceStore#isDefault(java.lang.String)
	 */
	public boolean isDefault(String name) {
		return prefs.isDefault(name);
	}

	/**
	 * Returns whether the current values in this property store
	 * require saving.
	 *
	 * @return <code>true</code> if at least one of the preferences
	 * known to this store has a current value different from its
	 * default value, and <code>false</code> otherwise
	 * @see org.eclipse.jface.preference.IPreferenceStore#needsSaving()
	 */
	public boolean needsSaving() {
		return prefs.needsSaving();
	}

	/**
	 * Sets the current value of the preference with the given name to
	 * the given string value.
	 * <p>
	 * This method is provided on this interface to simplify the implementation
	 * of decorators, and does not report a property change event.
	 * Normal clients should instead call <code>setValue</code>.
	 * </p>
	 *
	 * @param name the name of the preference
	 * @param value the new current value of the preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#putValue(java.lang.String, java.lang.String)
	 */
	public void putValue(String name, String value) {
		prefs.setValue(name, value);
	}

	/**
	 * Removes the given listener from this preference store.
	 * Has no affect if the listener is not registered.
	 *
	 * @param listener a property change listener
	 * @see org.eclipse.jface.preference.IPreferenceStore#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		// ignored
	}

	/**
	 * Sets the default value for the double-valued preference with the
	 * given name.
	 * <p>
	 * Note that the current value of the preference is affected if
	 * the preference's current value was its old default value, in which
	 * case it changes to the new default value. If the preference's current
	 * is different from its old default value, its current value is
	 * unaffected. No property change events are reported by changing default
	 * values.
	 * </p>
	 *
	 * @param name the name of the preference
	 * @param value the new default value for the preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, double)
	 */
	public void setDefault(String name, double value) {
		prefs.setDefault(name, value);
	}

	/**
	 * Sets the default value for the float-valued preference with the
	 * given name.
	 * <p>
	 * Note that the current value of the preference is affected if
	 * the preference's current value was its old default value, in which
	 * case it changes to the new default value. If the preference's current
	 * is different from its old default value, its current value is
	 * unaffected. No property change events are reported by changing default
	 * values.
	 * </p>
	 *
	 * @param name the name of the preference
	 * @param value the new default value for the preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, float)
	 */
	public void setDefault(String name, float value) {
		prefs.setDefault(name, value);
	}

	/**
	 * Sets the default value for the integer-valued preference with the
	 * given name.
	 * <p>
	 * Note that the current value of the preference is affected if
	 * the preference's current value was its old default value, in which
	 * case it changes to the new default value. If the preference's current
	 * is different from its old default value, its current value is
	 * unaffected. No property change events are reported by changing default
	 * values.
	 * </p>
	 *
	 * @param name the name of the preference
	 * @param value the new default value for the preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, int)
	 */
	public void setDefault(String name, int value) {
		prefs.setDefault(name, value);
	}

	/**
	 * Sets the default value for the long-valued preference with the
	 * given name.
	 * <p>
	 * Note that the current value of the preference is affected if
	 * the preference's current value was its old default value, in which
	 * case it changes to the new default value. If the preference's current
	 * is different from its old default value, its current value is
	 * unaffected. No property change events are reported by changing default
	 * values.
	 * </p>
	 *
	 * @param name the name of the preference
	 * @param value the new default value for the preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, long)
	 */
	public void setDefault(String name, long value) {
		prefs.setDefault(name, value);
	}

	/**
	 * Sets the default value for the string-valued preference with the
	 * given name.
	 * <p>
	 * Note that the current value of the preference is affected if
	 * the preference's current value was its old default value, in which
	 * case it changes to the new default value. If the preference's current
	 * is different from its old default value, its current value is
	 * unaffected. No property change events are reported by changing default
	 * values.
	 * </p>
	 *
	 * @param name the name of the preference
	 * @param value the new default value for the preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, java.lang.String)
	 */
	public void setDefault(String name, String defaultObject) {
		prefs.setDefault(name, defaultObject);
	}

	/**
	 * Sets the default value for the boolean-valued preference with the
	 * given name.
	 * <p>
	 * Note that the current value of the preference is affected if
	 * the preference's current value was its old default value, in which
	 * case it changes to the new default value. If the preference's current
	 * is different from its old default value, its current value is
	 * unaffected. No property change events are reported by changing default
	 * values.
	 * </p>
	 *
	 * @param name the name of the preference
	 * @param value the new default value for the preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, boolean)
	 */
	public void setDefault(String name, boolean value) {
		prefs.setDefault(name, value);
	}

	/**
	 * Sets the current value of the preference with the given name back
	 * to its default value.
	 * <p>
	 * Note that the preferred way of re-initializing a preference to the
	 * appropriate default value is to call <code>setToDefault</code>.
	 * This is implemented by removing the named value from the store,
	 * thereby exposing the default value.
	 * </p>
	 *
	 * @param name the name of the preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#setToDefault(java.lang.String)
	 */
	public void setToDefault(String name) {
		prefs.setToDefault(name);
	}

	/**
	 * Sets the current value of the double-valued preference with the
	 * given name.
	 * <p>
	 * A property change event is reported if the current value of the
	 * preference actually changes from its previous value. In the event
	 * object, the property name is the name of the preference, and the
	 * old and new values are wrapped as objects.
	 * </p>
	 * <p>
	 * Note that the preferred way of re-initializing a preference to its
	 * default value is to call <code>setToDefault</code>.
	 * </p>
	 *
	 * @param name the name of the preference
	 * @param value the new current value of the preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, double)
	 */
	public void setValue(String name, double value) {
		prefs.setValue(name, value);
	}

	/**
	 * Sets the current value of the float-valued preference with the
	 * given name.
	 * <p>
	 * A property change event is reported if the current value of the
	 * preference actually changes from its previous value. In the event
	 * object, the property name is the name of the preference, and the
	 * old and new values are wrapped as objects.
	 * </p>
	 * <p>
	 * Note that the preferred way of re-initializing a preference to its
	 * default value is to call <code>setToDefault</code>.
	 * </p>
	 *
	 * @param name the name of the preference
	 * @param value the new current value of the preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, float)
	 */
	public void setValue(String name, float value) {
		prefs.setValue(name, value);
	}

	/**
	 * Sets the current value of the integer-valued preference with the
	 * given name.
	 * <p>
	 * A property change event is reported if the current value of the
	 * preference actually changes from its previous value. In the event
	 * object, the property name is the name of the preference, and the
	 * old and new values are wrapped as objects.
	 * </p>
	 * <p>
	 * Note that the preferred way of re-initializing a preference to its
	 * default value is to call <code>setToDefault</code>.
	 * </p>
	 *
	 * @param name the name of the preference
	 * @param value the new current value of the preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, int)
	 */
	public void setValue(String name, int value) {
		prefs.setValue(name, value);
	}

	/**
	 * Sets the current value of the long-valued preference with the
	 * given name.
	 * <p>
	 * A property change event is reported if the current value of the
	 * preference actually changes from its previous value. In the event
	 * object, the property name is the name of the preference, and the
	 * old and new values are wrapped as objects.
	 * </p>
	 * <p>
	 * Note that the preferred way of re-initializing a preference to its
	 * default value is to call <code>setToDefault</code>.
	 * </p>
	 *
	 * @param name the name of the preference
	 * @param value the new current value of the preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, long)
	 */
	public void setValue(String name, long value) {
		prefs.setValue(name, value);
	}

	/**
	 * Sets the current value of the string-valued preference with the
	 * given name.
	 * <p>
	 * A property change event is reported if the current value of the
	 * preference actually changes from its previous value. In the event
	 * object, the property name is the name of the preference, and the
	 * old and new values are wrapped as objects.
	 * </p>
	 * <p>
	 * Note that the preferred way of re-initializing a preference to its
	 * default value is to call <code>setToDefault</code>.
	 * </p>
	 *
	 * @param name the name of the preference
	 * @param value the new current value of the preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, java.lang.String)
	 */
	public void setValue(String name, String value) {
		prefs.setValue(name, value);
	}

	/**
	 * Sets the current value of the boolean-valued preference with the
	 * given name.
	 * <p>
	 * A property change event is reported if the current value of the
	 * preference actually changes from its previous value. In the event
	 * object, the property name is the name of the preference, and the
	 * old and new values are wrapped as objects.
	 * </p>
	 * <p>
	 * Note that the preferred way of re-initializing a preference to its
	 * default value is to call <code>setToDefault</code>.
	 * </p>
	 *
	 * @param name the name of the preference
	 * @param value the new current value of the preference
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, boolean)
	 */
	public void setValue(String name, boolean value) {
		prefs.setValue(name, value);
	}

}
