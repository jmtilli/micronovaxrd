% tested and works

% theta is the absolute angle

%polarization_s = 1;
%polarization_p = 0;

% lambda, polarization (1 for s, 0 for p)
% thickness, spacing (perpendicular to surface), susceptibility ([x0 xh xhinv]) and grazing angle of incindence
% theta is row vector. all the other are column vectors.
function R = XRDCurve(lambda, d, zspace, suscdata, theta, stddevrad, thetaoffset)

    theta_B = asin(lambda./(2*zspace)); % Bragg's law. Order of reflection already included in zspace

	% mx1 vectors

    chi0 = suscdata(:,1);
	chih = suscdata(:,2);
	chihinv = suscdata(:,3);

	ntheta = length(theta);
	nlayers = length(d);

    if exist('thetaoffset','var')
        theta = theta + thetaoffset; % it isn't actually theta but omega in the measurement file
    end

	% ----- This is the real simulation code. The original equations are in Bartels-Hornstra-Lobeek ----- 


	% direction cosines WRT inner surface normal
	% (or doesn't matter if they are measured WRT outer normal instead, since we get the same results)
    % however, we now use gamma0 

    % not used anywhere
    %gamma0 = sin(theta_B(end));
    %gammah = -gamma0;

	% Bartels-Hornstra-Lobeek, Wormington et al, Wang et al
	% this is negative for Bragg reflection since the signs are opposite
    % XXX: should it be this or sin(theta_B+thetaoffset)/sin(theta_B-thetaoffset)
    % not used, since it deviates very little from b = -1
    b = -sin(theta_B-thetaoffset)./sin(theta_B+thetaoffset);
    %b = -ones(size(theta_B));




	% Since theta_B is calculated for each layer, this is an mxn matrix

    % unoptimized
	%alphah = -4*(ones(nlayers,1)*sin(theta)-sin(theta_B)*ones(1,ntheta)).*(sin(theta_B)*ones(1,ntheta));
    % optimized
	alphah = -4*(ones(nlayers,1)*sin(theta)-sin(theta_B)*ones(1,ntheta)).*(sin(theta_B)*ones(1,ntheta));

	% an approximation given in many articles (not vectorized):
	% eta = -2*(theta - theta_b) * sin(2*theta_b)

    R1 = zeros(size(theta));

    % is it -alphah or alphah? probably b*alphah == -alphah
    % try both alphah and -alphah here! try also b==-1 and b==1 here.

    % since this is a complex number, there are two possibilities where the error is:
    % 1. eta0 does not trace the right curve as a function of alphah
    % 2. eta0 is calculated correctly and the error is in the recursive algorithm
    % ... and 3. alphah is calculated incorrectly since it is different for each layer
    eta0 = ((b*ones(size(theta))).*alphah + (chi0.*(1-b))*ones(size(theta))) ./ (2*(sqrt(chih.*chihinv).*sqrt(abs(b))) * ones(size(theta)));

    % ... testing
    % one of these might work:
    %eta0 = sqrt(1) * (2*chi0*ones(size(theta)) + alphah) ./ (2*sqrt(chih.*chihinv) * ones(size(theta)));
    %eta0 = sqrt(1) * (-2*chi0*ones(size(theta)) + alphah) ./ (2*sqrt(chih.*chihinv) * ones(size(theta)));
    %eta0 = sqrt(-1) * (2*chi0*ones(size(theta)) + alphah) ./ (2*sqrt(chih.*chihinv) * ones(size(theta))); % NO
    %eta0 = sqrt(-1) * (-2*chi0*ones(size(theta)) + alphah) ./ (2*sqrt(chih.*chihinv) * ones(size(theta))); % NO
    %eta0 = sqrt(1) * (2*chi0*ones(size(theta)) - alphah) ./ (2*sqrt(chih.*chihinv) * ones(size(theta)));
    %eta0 = sqrt(1) * (-2*chi0*ones(size(theta)) - alphah) ./ (2*sqrt(chih.*chihinv) * ones(size(theta)));
    %eta0 = sqrt(-1) * (2*chi0*ones(size(theta)) - alphah) ./ (2*sqrt(chih.*chihinv) * ones(size(theta))); % NO
    %eta0 = sqrt(-1) * (-2*chi0*ones(size(theta)) - alphah) ./ (2*sqrt(chih.*chihinv) * ones(size(theta))); % NO

    % eta = eta0 / C

    %Tmatrix = pi*(sqrt(chih.*chihinv).*d)*ones(size(gamma0)) ./ (lambda*ones(size(chih))*gamma0);

    for spol=0:1

        %--------------- begin for block --------------

        % Some articles use the cos(2*theta_Bs) approximation instead of this
        % XXX: what about abs?
        % we make an approximation here; the error is small enough so this won't be a problem
        %C = ones(1,ntheta)*spol + not(spol)*abs(cos(2*theta));
        C = spol + not(spol)*abs(cos(2*theta_B));

        % Wormington et al
        %eta = (alphah + chi0*(1-b)) ./ (2*C*sqrt(chih.*chihinv) * sqrt(abs(b)));
        % Wang et al, Bartels-Hornstra-Lobeek

        % unoptimized
        %eta = ((ones(nlayers,1)*b).*alphah + chi0*(1-b)) ./ (2*sqrt(chih.*chihinv) * (sqrt(abs(b)).*C));
        % optimized
        %eta = (-alphah + chi0*(1-b)) ./ (2*sqrt(chih.*chihinv) * C);
        %eta = eta0 ./ (ones(size(chih))*C);
        eta = eta0 ./ (C*ones(size(theta)));

        etasubstr = eta(nlayers,:);

        % Wormington et al (wrong!)
        %X = (etasubstr - sign(real(etasubstr)).*(etasubstr.^2-1));
        % Wang et al (verified and works correctly), Bartels-Hornstra-Lobeek
        % Darwin-Prins formula
        X = (etasubstr - sign(real(etasubstr)).*sqrt(etasubstr.^2-1));

        %sqrtmatrix = sqrt(eta.^2-1);

        for k=(nlayers-1):-1:1
            etak = eta(k,:);
            % Wormington et al, Wang et al, Bartels-Hornstra-Lobeek (4 following equations:)
            % XXX: does Bartels-Hornstra-Lobeek have abs in sqrt(abs(gamma0.*gammah))?
            % We get incorrect results without abs so it probably has.

            % unoptimized
            %T = pi*C*sqrt(chih(k)*chihinv(k))*d(k) ./ (lambda*sqrt(abs(gamma0.*gammah)));
            % optimized
            %T = pi*C*sqrt(chih(k)*chihinv(k))*d(k) ./ (lambda*gamma0);

            % approximated
            %T = pi*C*sqrt(chih(k)*chihinv(k))*d(k) ./ (lambda*sin(theta_B(k)));
            % not approximated
            T = pi*C(k)*sqrt(chih(k)*chihinv(k))*d(k) ./ (lambda*sqrt(sin(theta_B(k)+thetaoffset)*sin(theta_B(k)-thetaoffset)));

            %T = Tmatrix(k,:).*C;

            sqrtterm = sqrt(etak.^2-1);
            %sqrtterm = sqrtmatrix(k,:);

            expfactor = exp(-i*T.*sqrtterm);
            %expfactor = exp(-i*Tmatrix(k,:).*sqrtterm);
            %expfactor = exp(-i*(pi*C*sqrt(chih(k)*chihinv(k))*d(k) ./ (lambda*gamma0)).*sqrtterm);

            S1=(X - etak + sqrtterm).*expfactor;
            S2=(X - etak - sqrtterm)./expfactor;
            X = etak + sqrtterm .* (S1 + S2) ./ (S1 - S2);
        end
        R1 = R1 + 0.5*abs(X).^2;

    end

    % --------end for block----------


	% TODO: are the results different? (not in this case)
	% Wormington et al
	%R1 = abs(X).^2;
	% Wang et al
	% XXX: don't know which chih and chihinv to use
	R2 = abs(chih(1)/chihinv(1))*R1;
	R = R1;

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
