<div class="box">
	<span>Response</span>
	<ul>
		<li>ResponseCode: ${responseCode}</li>
		<#if payload??>
			<li>
				<div>payload:</div>
				${payload.expected}
			</li>
		</#if>
    </ul>
</div>