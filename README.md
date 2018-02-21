# Floaty

<img src="https://github.com/kettsun0123/Floaty/blob/master/arts/logo.png">

## Gradle

```groovy

dependencies {
    compile 'com.github.kettsun0123:floaty:0.1.0'
}

```
<br/>

<img src="https://github.com/kettsun0123/Floaty/blob/master/arts/replace.gif" align="right" width="30%">

## Features
![Platform](http://img.shields.io/badge/platform-android-green.svg?style=flat)
![Download](https://api.bintray.com/packages/kettsun0123/maven/floaty/images/download.svg)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![API](https://img.shields.io/badge/API-13%2B-brightgreen.svg?style=flat)

This is an Android Library for bottom view with custom transition.
Floaty easily provides a replacing with transition feature. 
<br/>
- [x] support custom layout view.
- [x] support in-out animation like snackbar. 
- [x] transition between views when called `replace()`
- [x] sample code.
- [ ] support other gravities.
- [ ] update README(add api documents).
- [ ] and more...

<br/>
<br/>
<br/>
<br/>

## Usage
Create Floaty layout file.

```xml
<uno.ketts.floaty.FloatyContentLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/layout_floaty_content"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_gravity="bottom"
    android:background="@color/whisper"
    android:theme="@style/ThemeOverlay.AppCompat.Dark">

    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:background="@color/whisper"
        tools:ignore="HardcodedText">

        <TextView
            android:id="@+id/text_floaty_small"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Small one"
            android:textSize="14sp"
            android:textColor="@android:color/black"
            android:layout_centerInParent="true"/>

    </RelativeLayout>
</uno.ketts.floaty.FloatyContentLayout>
```

<strong>`FloatyContentLayout` extend FrameLayout.</strong> Make sure to add ViewGroup.

<br/>

and use in activity file.

```kotlin
button_floaty.setOnClickListener {
            Floaty.make(it, R.layout.layout_floaty_big).setDuration(Floaty.LENGTH_INDEFINITE).show()
        }
```

<br/>
<br/>

When you want to replace view with transition,

```kotlin
button_floaty_replace.setOnClickListener {
            Floaty.make(it, R.layout.hugahuga).setDuration(Floaty.LENGTH_SHORT).setTransition(CustomTransition()).replace()
        }
```

## Sample
Clone this repo and check out the [app](https://github.com/kettsun0123/Floaty/tree/master/example) module.

## Change Log

### Version: 0.1.0

  * beta release


## Author

* **Yuji Koketsu**
    * **Github** - (https://github.com/kettsun0123)
    * **Twitter** - (https://twitter.com/kettsun0123)
    * **Facebook** - (https://www.facebook.com/y.kettsun)
    
## Thanks


## Licence
```
Copyright 2018 Yuji Koketsu.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```