
// Faz animação para subir tela
jQuery('#save').click(function(event) {
	console.log('>> Voltar ao topo')
	event.preventDefault();
	jQuery('html, body').animate({
		scrollTop : 0
	}, 800);  
	return false;
})