<!-- INCLUDE OF HEADER -->
<?php 
    $page="home";
    include("header.php");
?>

<!-- ULTIMA ORA - THIS ELEMENT IS HIDDEN TO DEFAULT -->
<div class="row" style="display: none;">
        <?php include("ultimaOra.php");?>
</div>
<div class="row">
<!-- COLUMNS  SX -->
    <div class="large-8 medium-6 column columnLeft">
        <!-- SEARCH LIST RESULT-->
        <?php include("pageLinksList.php");?>
    </div>
<!-- COLUMNS  DX -->
    <div class="large-4 medium-6 column columnRight">
        <!-- EDICOLA -->
        <?php include("edicola.php");?>
        
        <!-- DIRETTA -->
        <?php include("diretta.php");?>
        
        <!-- IN BREVE -->
        <?php include("inBreve.php");?>
       
        <!-- FOTO GALLERY -->
        <?php include("fotoGallery.php");?>
        
        <!-- VIDEO GALLERY -->
        <?php include("videoGallery.php");?>
        
        <!-- VISTO E PIACIUTO -->
        <?php include("vistoPiaciuto.php");?>
        
        <!-- NO TITLE VIDEO -->
        <?php include("noTitleVideo.php");?>
        <!-- PARTNERS -->
        <?php include("partners.php");?>
    </div>
</div>
<!-- INCLUDE OF FOOTER -->
<?php 
    $page="home";
    include("footer.php");
?>