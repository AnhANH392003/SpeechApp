<br />
<p align="center">
  <a href="https://github.com/Tuanna2208/Speech-Processing-Project">
    <img src="assets/logo.png" alt="Logo" width="100" height="100">
  </a>

  <h3 align="center">Speech Processing Android</h3>

  <p align="center">
    Application Android Assistant
    <br />
    <br />
    <!-- <a href="assets/logo.png">View Demo</a>
    ·
    <a href="https://github.com/Tuanna2208/Speech-Processing-Project/issues">Report Bug</a>
    ·
    <a href="https://github.com/Tuanna2208/Speech-Processing-Project/issues">Request Feature</a> -->
  </p>
</p>

## Table of Contents

* About project
* Prerequisites
* Support
* Run App in Android Studio
* Getting start

## About project 

Các công nghệ chính được sử dụng trong phần mềm:
    
* Sử dụng [Trigger Word(Wake-up)]() dựa trên thư viện PocketSphinx Android  
* Speech To Text và Text To Speech trên nền tảng API Google
* Các tính năng trợ lý trên Android:
    
    * Đặt báo thức
    * Đếm ngược
    * Gọi điện thoại
    * Tìm địa chỉ trên Google Map
    * Mở các ứng dụng trên điện thoại
    * Thời tiết
    * Search trên công cụ tìm kiếm google

## Rrerequisites

* Android SDK v24 (minSdkVersion 19 - targetSdkVersion 28)
* Latest Android Build Tools
* Android Support Repository

## Support 

### Wake-up(hotword)

[![Trigger word](http://img.youtube.com/vi/9PrLULZp4UQ/0.jpg)](http://www.youtube.com/watch?v=9PrLULZp4UQ "")

Làm thế nào để mở ứng dụng trên Android bằng giọng nói giống như "Oke Goole" để mở Google Assistant. Trong ứng dụng Android đã được khởi chạy và đang ở chế độ chạy nền, ứng dụng chỉ chờ người dùng nói từ "hey okay"(từ hot word được cài đặt trước) thì ứng dụng sẽ chạy để bạn tiếp tục các trải nghiệm.

Chúng ta không thể nào xác định chính xác thời gian cần chờ đợi cho đến khi từ hot word được nói, có nghĩa là cách sử dụng nhận dạng giọng nói trực tuyến (vd: từ API Google) là không hợp lý. Hiện nay đã có PocketSphinx, một công cụ nhận dạng giọng nói có thể cài đặt trên các thiết bị di động hoạt động cục bộ trên điện thoại mà không cần kết nối Internet

Bạn có thể tham khảo thêm tại: [wolfpaulus](https://wolfpaulus.com/mac/custom-wakeup-words-for-an-android-app/) hoặc [github cmusphinx](https://github.com/cmusphinx/pocketsphinx-android-demo)

#### Thêm hotword
Trong folder từ điển `src/main/assets/sync/models/lm` chứa tất cả các từ mà bạn muốn nhận ra. Một bảng băm MD5 được tạo và lưu trữ, bảng băm MD5 này cần được cập nhật mỗi khi bạn thay đổi bộ từ điển hotwords(Vd: sử dụng http://passwordsgenerator.net/md5-hash-generator/)

Đây là bộ từ điển để nhận ra các từ {hey, okay, john, george, paul, ringo, stop}

```
george  JH AO R JH
hey HH EY
john    JH AA N
okay	OW K EY
paul P AO L
ringo   R IY NG G OW
stop	S T AA P
```

Bạn có thể tạo ra cách phát âm cho từng từ trong từ điển bằng cách sử dụng **g2p-seq2seq**, nó sử dụng mô hình biến áp từ bộ công cụ tenor2tensor Một mô hình LSTM 2 lớp tiếng Anh với 512 đơn vị ẩn có sẵn trên trang web. Bạn có thể tham khảo hướng dẫn [tại đây.](https://cmusphinx.github.io/wiki/tutorialdict/#using-g2p-seq2seq-to-extend-the-dictionary)

### Text To Speech và Speech To Text trên API Google

Google cung cấp các API cho phép sử dụng các chức năng TextToSpeech và SpeechToText một cách đơn giản và hiệu quả
[(Link tham khảo)](https://developer.android.com/reference/android/speech/tts/TextToSpeech)

Ưu điểm:
* Nhận dạng giọng nói tương đối nhanh và chính xác
* Dễ dàng tích hợp và sử dụng trên thiết bị Android
* Được sử dụng miễn phí và được update liên tục từ Google

Nhược điểm:
* Tốc độ sử lý phụ thuộc vào tốc độ đường chuyền internet trên điện thoại
* Quá phụ thuộc vào kết quả từ Google API
* TextToSpeech của Google đọc chưa có ngữ điệu nên khá khó nghe
* SpeechToText yêu cầu minSDKVersion>=21 và điện thoại có hỗ trợ đọc tiếng Việt

## Run App in Android Studio

1. Clone repo từ github

```git clone https://github.com/thangtran480/SpeechApp.git```

2. Mở project SpeechApp trên Android Studio
3. Run app trên máy ảo hoặc máy thật

## Screen

![main screen](assets/image1.jpg)
![main screen](assets/image2.jpg)
![main screen](assets/image3.jpg)

## Hướng dẫn sử dụng

Đặt báo thức

* Đặt báo thức lúc {giờ cần đặt báo thức}

![bao thuc](assets/gif/bao-thuc.gif)

Đếm ngược

* Đặt đếm ngược {thời gian cần đếm ngược}

![dem nguoc](assets/gif/timer.gif)

Gọi điện thoại

* Gọi số {số điện thoại}
* Gọi cho {tên trong danh bạ}
* Gọi cho taxi (mặc định gọi taxi G7 sdt: 024 3232 3232)

![goi so](assets/gif/goi-so.gif)

![goi ten](assets/gif/goi-ten.gif)

Google map

* Tìm đường đến {địa điểm}

![tim duong](assets/gif/tim-duong.gif)
* Tìm {địa điểm} gần nhất

![tim gan nhat](assets/gif/tim-gan.gif)

Mở ứng dụng trên điện thoại 

* Mở {tên app}: chụp ảnh, Facebook, Youtube, Nghe nhạc(MP3), Messenger, Instagram, Google Map (tên tất cả các app có trên điện thoại)

![mo app](assets/gif/mo-facebook.gif)

Weather: chỉ show thời tiết vị trí hiện tại
    
* Thời tiết hôm nay thế nào?

![thoi tiet](assets/gif/thoi-tiet.gif)

Tìm kiếm bằng Google search:
* Hôm nay có sự kiện gì?
* Bà Tân Vlog là ai?

![search google](assets/gif/search-google.gif)

