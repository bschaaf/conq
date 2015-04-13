<!-- INCLUDE OF HEADER -->
<?php 
    $page="home";
    include("header.php");
?>

<!-- ULTIMA ORA - THIS ELEMENT IS HIDDEN TO DEFAULT -->
<div class="row" style="display: none;">
    <?php include("ultimaOra.php");?>
</div>

<!-- HOME 2 - EXTENDED
 <div class="row"> 
     <!-- HOME EXTENDED
        <?php //include("homeExtended.php");?>
 </div>
-->

<!-- CENTER HORIZON COLUMN -->
 <div class="row openColumn"> 
     <!-- HOME -->
        <?php include("homeOpen.php");?>
 </div>

<div class="row">
    <!-- EDICOLA GALLERY-->
    <!-- Preview cover-->
     <?php include("anteprimaEdicola.php");?>
    <!-- Search forms -->
     <?php include("ricercaEdicola.php");?>
    <!-- Edicola Sliders -->
     <?php include("EdicolaSlider.php");?>
</div>
<!-- INCLUDE OF FOOTER -->
<?php 
    $page="home";
    include("footer.php");
?>