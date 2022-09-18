
const ws = new WebSocket(`ws://${location.host}/comembus/ComembusWebSocket`); 

ws.addEventListener('open', (e) => console.log('open : ', e));
ws.addEventListener('error', (e) => console.log('error : ', e));
ws.addEventListener('close', (e) => console.log('close : ', e));
ws.addEventListener('message', (e) => {
	console.log('message : ', e);
	const {messageType,  data : {msg}, time} = JSON.parse(e.data);
	
	const wrapper = document.querySelector("#notification");
	const blackBell = wrapper.querySelector("#blackBell");
	switch(messageType){
		case 'NEW_COMMENT' :
			blackBell.style.color = 'red';
			$(blackBell).one('click', () => {
				alert(msg);
				blackBell.style.color = 'inherit';
			});
			break;
		case 'NEW_APPLICANT' :
			blackBell.style.color = 'red';
			$(blackBell).one('click', () => {
				alert(msg);
				blackBell.style.color = 'inherit';
			});
			break;
		case 'APPLY_RESULT' :
			blackBell.style.color = 'red';
			$(blackBell).one('click', () => {
				alert(msg);
				blackBell.style.color = 'inherit';
			});
			break;
		case 'APPLY_CANCELED':
			blackBell.style.color = 'red';
			$(blackBell).one('click', () => {
				alert(msg);
				blackBell.style.color = 'inherit';
			});
			break;
	}
	
		
});