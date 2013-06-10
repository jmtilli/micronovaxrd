% theta is the absolute angle

%polarization_s = 1;
%polarization_p = 0;

% spol must be exactly 0 or 1. For mixed, 0 and 1 must be calculated separately.
% spol must never be set to 0.5!
% TODO: mixed polarization. We must do the calculations for mixed polarization before convolving with a gaussian.

% lambda, polarization (1 for s, 0 for p)
% thickness, spacing (perpendicular to surface), susceptibility ([x0 xh xhinv]) and grazing angle of incindence
% theta is row vector. all the other are column vectors.
function R = XRDCurveMIN(lambda, d, zspace, suscdata, theta, stddevrad)

    theta_B = asin(lambda./(2*zspace)); % Bragg's law. Order of reflection already included in zspace

	% mx1 vectors

    chi0 = suscdata(:,1);
	chih = suscdata(:,2);
	chihinv = suscdata(:,3);

	ntheta = length(theta);
	nlayers = length(d);


	% ----- This is the real simulation code. The original equations are in Bartels-Hornstra-Lobeek ----- 

	% Some articles use the cos(2*theta_Bs) approximation instead of this
    % XXX: what about abs?


	% direction cosines WRT inner surface normal
	% (or doesn't matter if they are measured WRT outer normal instead, since we get the same results)
	gamma0 = sin(theta);
	gammah = -sin(theta);

	% Bartels-Hornstra-Lobeek, Wormington et al, Wang et al
	% this is negative for Bragg reflection since the signs are opposite
	b = gamma0./gammah;

    R1 = zeros(size(theta));

    for spol=0:1
        C = ones(1,ntheta)*spol + not(spol)*abs(cos(2*theta));


        % Since theta_B is calculated for each layer, this is an mxn matrix
        alphah = -4*(ones(nlayers,1)*sin(theta)-sin(theta_B)*ones(1,ntheta)).*(sin(theta_B)*ones(1,ntheta));
        % an approximation given in many articles (not vectorized):
        % eta = -2*(theta - theta_b) * sin(2*theta_b)


        % Wormington et al
        %eta = (alphah + chi0*(1-b)) ./ (2*C*sqrt(chih.*chihinv) * sqrt(abs(b)));
        % Wang et al, Bartels-Hornstra-Lobeek
        eta = ((ones(nlayers,1)*b).*alphah + chi0*(1-b)) ./ (2*sqrt(chih.*chihinv) * (sqrt(abs(b)).*C));

        etasubstr = eta(nlayers,:);

        % Wormington et al (wrong!)
        %X = (etasubstr - sign(real(etasubstr)).*(etasubstr.^2-1));
        % Wang et al (verified and works correctly), Bartels-Hornstra-Lobeek
        % Darwin-Prins formula
        X = (etasubstr - sign(real(etasubstr)).*sqrt(etasubstr.^2-1));

        for k=(nlayers-1):-1:1
            etak = eta(k,:);
            % Wormington et al, Wang et al, Bartels-Hornstra-Lobeek (4 following equations:)
            % XXX: does Bartels-Hornstra-Lobeek have abs in sqrt(abs(gamma0.*gammah))?
            % We get incorrect results without abs so it probably has.
            T = pi*C*sqrt(chih(k)*chihinv(k))*d(k) ./ (lambda*sqrt(abs(gamma0.*gammah)));
            S1=(X - etak + sqrt(etak.^2-1)).*exp(-i*T.*sqrt(etak.^2-1));
            S2=(X - etak - sqrt(etak.^2-1)).*exp(+i*T.*sqrt(etak.^2-1));
            X = etak + sqrt(etak.^2-1) .* (S1 + S2) ./ (S1 - S2);
        end
        R1 = R1 + 0.5*abs(X).^2;
    end


	% TODO: are the results different? (not in this case)
	% Wormington et al
	%R1 = abs(X).^2;
	% Wang et al
	% XXX: don't know which chih and chihinv to use
	R2 = abs(chih(1)/chihinv(1))*R1;
	R = R2;

    % Instrument convolution
    stddevs = 4;

    dthetarad = (theta(end)-theta(1))/(length(theta)-1);
    gfilt = gaussian_filter(dthetarad, stddevrad, stddevs);
    if(length(gfilt) > 1)
    %    if(!all(abs(alpha_0-linspace(alpha_0(1),alpha_0(end),length(alpha_0))) <   linthreshold*dalpha0rad))
    %        error("alpha_0 not uniformly spaced");
    %    end
        R = apply_odd_filter(gfilt, R);
    end

end
