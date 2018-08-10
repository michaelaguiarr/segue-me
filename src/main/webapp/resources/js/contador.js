		/*
		 * funcoes que trabalham como um contador do tempo de sessao
		 * do usuario.
		 */
		var tempoPassado=0;
		var funcaoTimeout;
		 
		/* tempo maximo de sessao: 7 minutos. */
		var tempoMaximoSessao=420000;
		var tempoAtualizacao=1000;
		 
		window.onload=function(){
		  contarTempo();
		}
		 
		function contarTempo()
		{
		  var tempoEmSegundo=tempoMaximoSessao/1000 - tempoPassado;
		  var tempoEmMinutos=parseInt(tempoEmSegundo/60);
		  var tempoEmSegundoFinal=tempoEmSegundo%60;

		  if(tempoEmSegundoFinal == 0){
			  tempoEmSegundoFinal = "0"+tempoEmSegundoFinal;
			}
		// Cria a variável para formatar no estilo hora/cronômetro
		  var horaImprimivel = '0' + tempoEmMinutos + 'min' + tempoEmSegundoFinal;
		  document.getElementById('contadorSessao').value=horaImprimivel;
		  tempoPassado=tempoPassado+1;
		
		  //se ultrapassar o tempo maximo, redirecione o usuario para a pagina de login.
		  //unidades: segundos.
		  if(tempoPassado > tempoMaximoSessao/1000){
		    alert('Sua sessão expirou. Você será redirecionado para a página de login.');
		    location.href="/segue-me/logout";
		  }
		   
		  funcaoTimeout=setTimeout("contarTempo()",tempoAtualizacao);
		}
		 
		function resetaContador()
		{
		  tempoPassado=0;
		  clearTimeout(funcaoTimeout);
		  contarTempo();
		}
