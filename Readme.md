# Speech Processing in Android 

Application Assignment in Android with Speech to Text API Google


## How a wake-up word (hotword) 

This projects emonstrate, how a wake-up word (a.k.a. hot word) can be used inside an android app, to wake it up. Imagine a scenario, where the android app is already launched and running in the foreground, just waiting for a user to say the wake-up word or phrase, to start the full experience, i.e., start the next activity.

Waiting is somewhat indeterministic, we don’t really know how long we have to wait, until the wake-up word gets spoken, which means using an on-line speech recognition service doesn’t sound like a good idea. Fortunately, there is [PocketSphinx](https://github.com/cmusphinx/pocketsphinx), a lightweight speech recognition engine, specifically tuned for handheld and mobile devices that works locally on the phone. Let’s get started, by creating a simple project with Android Studio.

More details are available here: https://wolfpaulus.com/mac/custom-wakeup-words-for-an-android-app/ or https://github.com/cmusphinx/pocketsphinx-android-demo

### Add hotword to project 

Inside project’s src/main folder, create a directory path like this assets/sync/models/lm and store a dictionary, containing all the words you want to recognize. Again, an MD5 hash has be created and stored. Remember that the md5 hash needs to be updated, each time you make a change to the dictionary. (E.g. use http://passwordsgenerator.net/md5-hash-generator/)

Here for instance it a dictionary for recognizing the words {hey, okay, john, george, paul, ringo, stop}

```
george  JH AO R JH
hey HH EY
john    JH AA N
okay	OW K EY
paul P AO L
ringo   R IY NG G OW
stop	S T AA P
```


## How to work

Call phone
    
    Gọi số {số điện thoại}
    Gọi cho {tên trong danh bạ}

Map

    Tìm đường đến {địa điểm}
    Tìm {địa điểm} gần nhất

Lauching application

    Mở {tên app}: chụp ảnh, Facebook, Youtube, Nghe nhạc(MP3), Messenger, Instagram, Google Map

Weather: show thời tiết vị trí hiện tại
    
    Thời tiết hôm nay thế nào?

