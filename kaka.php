<?php
    class Books{
        // /Member Variables/
        var $price;
        var $title;

        // /Member Function/
        function setPrice($par) {
            $this->price = $par;
        }

        function getPrice(){
            echo $this->price . "<br>";
        }

        function setTitle($par){
            $this->title = $par;
        }

        function getTitle(){
            echo $this->title . "<br>";
        }
    }
    
    // Membuat Object
    $science = new Books();
    // Menambahkan Judul Buku
    $science->setTitle("Physics for High School");

    // Menambahkan Harga Buku
    $science->setPrice(100000);

    // Menampilkan Buku
    echo $science->getTitle(). "\n" . $science->getPrice(). "\n";
    // OR
    echo $science->getTitle(). "\n";
    echo $science->getPrice(). "\n";
?>