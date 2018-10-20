# Wombatik

> Dibuat untuk memperingati Hari Batik yang jatuh pada tanggal 2 Oktober

Wombatik adalah sebuah aplikasi kecil yang bertujuan untuk mempertahankan keaslian batik Indonesia. Aplikasi ini menggunakan teknik [steganografi](https://en.wikipedia.org/wiki/Steganography) (tepatnya, [_pixel indicator technique_](#pixel-indicator-technique)) untuk menyisipkan pesan tanda kepemilikan (selanjutnya akan disebut *watermark*) pada gambar batik Indonesia. Sehingga kedepannya, masalah asal-muasal suatu motif batik dapat diselesaikan hanya dengan melihat keberadaan *watermark* pada gambar motif batik tersebut.

Selain hal-hal diatas, _watermark_ akan menyelesaikan masalah-masalah klasik dalam dunia visual, seperti _content authentication_ dan _integrity_.

## Pixel Indicator Technique

> Supaya anda dapat memahami topik ini, ada baiknya bila anda memiliki pengetahuan dasar mengenai [_bit numbering_](https://en.wikipedia.org/wiki/Bit_numbering), [representasi gambar digital](<https://en.wikipedia.org/wiki/Channel_(digital_image)>),dan representasi [karakter _ASCII_](https://en.wikipedia.org/wiki/ASCII) terlebih dahulu.

Pada dasarnya, _pixel indicator technique_ merupakan teknik yang memanfaatkan _least significant bit_ untuk menyembunyikan _watermark_ pada gambar target (selanjutnya, akan disebut _carrier_) yang memiliki _channel_ RGB. Penjelasan mengenai teknik ini dapat dilihat melalui _flowchart_ dibawah ini

<h3><center>Menyisipkan <i>watermark</i></center>></h3>

<center>![algoritma_penyisipan](https://i.imgur.com/LQYUyox.png)</center>
