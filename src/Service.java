import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.discovery.EurekaClient;
import com.factor.compute.ComputeServices;
import com.factor.filter.ValidateToken;
import com.factor.pojo.AnswerObj;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value="factor", description="factor Service")
public class Service {

	@Autowired
	private Environment env;
	
	@Autowired
	private ComputeServices computeServices;
	
	@Autowired
	private EurekaClient discoveryClient;

	@Value("${perlservice.instance}")
	private String perlInstance;

	@Value("${tokenValidateService.instance}")
	private String validateServiceInstance;

	public static final Logger logger = Logger.getLogger(ComputeServices.class.getName());


	/** Returning HTTP Status code **/
	@CrossOrigin
	
	@ApiOperation(value = "Validates for the 12 Factors (5 factors through Checklist and 7 Factors through GIT Codebase).")
	@ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Missing or incorrect input."),
            @ApiResponse(code = 500, message = "System error. Please try after sometime.")
	})
	@PostMapping(value = "/{language}/assessment", produces = "application/json")
	@ResponseBody
	public ResponseEntity<Map<String, String>> submitAnswers(@ApiParam(value = "A Valid Authorization Token. Eg : 'Bearer : eyJhbGci...' ")
    														@RequestHeader(name="Authorization") String AuthorizationToken,
    														@ApiParam(value = "language of the Project that will be validated for 12 factors") 
    														@PathVariable String language,
    														HttpServletRequest request, @RequestBody AnswerObj validatorInput) {

		Map<String, String> responseMap = new HashMap<>();
		
		String perlUrl = null;
				

		//get from eureka if env variable null
		if (perlUrl == null || perlUrl.isEmpty()) {
			perlUrl = (discoveryClient.getNextServerFromEureka(perlInstance, false)).getHomePageUrl()
					+ "api/twelvefactors/validator";
		}
		logger.info("Perl Url: " + perlUrl);
		
		String validateServiceUri = null;
		validateServiceUri = env.getProperty("TOKEN_VALIDATE_SERVICE");
		
		if(validateServiceUri == null || validateServiceUri.isEmpty()) {
			validateServiceUri = (discoveryClient.getNextServerFromEureka(validateServiceInstance, false)).getHomePageUrl()
				+ "authtoken/validate";
		}
		logger.info("ValidateServiceURI: " + validateServiceUri);

		ResponseEntity<String> validateResponse = (new ValidateToken()).validate(request, validateServiceUri);
		int responseCode = validateResponse.getStatusCodeValue();
				
		if ((responseCode == 200) && (validatorInput != null)) {
			logger.info("repoURL: " + validatorInput.getRepoURL());
			logger.info("Answer Object Summary list size: " + validatorInput.getListSummary().size());
			JSONObject json = new JSONObject(validateResponse.getBody());
			validatorInput.setLoggedInUsername(json.getString("username"));
			
			responseMap = computeServices.compute(validatorInput, perlUrl);
			logger.info("responseMap: " + responseMap.size());
			responseCode = (responseMap.get("Code") != null) ? Integer.parseInt(responseMap.get("Code"))
					: validateResponse.getStatusCodeValue();
		}
		return new ResponseEntity<>(responseMap, HttpStatus.valueOf(responseCode));
	}
}