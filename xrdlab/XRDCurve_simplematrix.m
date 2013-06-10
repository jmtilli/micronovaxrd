% tested and works

% theta is the absolute angle

%polarization_s = 1;
%polarization_p = 0;

% lambda
% thickness, spacing (perpendicular to surface), susceptibility ([x0 xh xhinv]) and grazing angle of incindence
% theta is row vector. all the other are column vectors.
function R = XRDCurve_simplematrix(lambda, d, zspace, suscdata, theta, stddevrad, thetaoffset)

    theta_B = asin(lambda./(2*zspace)); % Bragg's law. Order of reflection already included in zspace

	% mx1 vectors

    chi0 = suscdata(:,1);
	chih = suscdata(:,2);
	chihinv = suscdata(:,3);

	ntheta = length(theta);
	nlayers = length(d);

    if exist('thetaoffset','var')
        theta = theta + thetaoffset;
    end

	% Since theta_B is calculated for each layer, this is an mxn matrix

    % unoptimized
	%alphah = -4*(ones(nlayers,1)*sin(theta)-sin(theta_B)*ones(1,ntheta)).*(sin(theta_B)*ones(1,ntheta));
    % optimized
	%alphah = 4*(sin(theta_B)*ones(1,ntheta) - ones(nlayers,1)*gamma0).*(sin(theta_B)*ones(1,ntheta));

    R1 = zeros(size(theta));

    for spol=0:1

        %--------------- begin for block --------------

        % XXX: what about abs?
        % we make an approximation here; the error is small enough so this won't be a problem
        %C = ones(1,ntheta)*spol + not(spol)*abs(cos(2*theta));
        C = spol + not(spol)*abs(cos(2*theta_B));

        gamma0 = sin(theta_B(end)-thetaoffset);
        gammah = sin(theta_B(end)+thetaoffset);

        % substrate
        alphah = 4*(sin(theta_B(end)) - sin(theta)).*sin(theta_B(end));
        q = pi*C(end)*sqrt(chih(end)*chihinv(end))./(lambda*sqrt(gamma0.*gammah));
        eta = pi./(2*lambda.*q) .* (chi0(end)./gamma0 + chi0(end)./gammah - alphah./gammah);
        % Darwin-Prins formula
        S0 = ones(size(theta));
        SH = (eta - sign(real(eta)).*sqrt(eta.^2-1));

        for k = (nlayers-1):(-1):1
            gamma0 = sin(theta_B(k)-thetaoffset);
            gammah = sin(theta_B(k)+thetaoffset);

            % defined in the article
            alphah = 4*(sin(theta_B(k)) - sin(theta)).*sin(theta_B(k));
            q = pi*C(k)*sqrt(chih(k)*chihinv(k))./(lambda*sqrt(gamma0.*gammah));
            eta = pi./(2*lambda.*q) .* (chi0(k)./gamma0 + chi0(k)./gammah - alphah./gammah);
            % we don't need this
            %xi = pi/(2*lambda.*q) .* (chi0(k)./gamma0 - chi0(k)./gammah + alphah./gammah);

            % don't calculate the same quantities too many times
            sq = sqrt(eta.^2-1);
            qtsq = (q*d(k)).*sq;
            sqtsqpsq = sin(qtsq)./sq; % sin(qtsq) per sq

            % defined in the article
            m11 = cos(qtsq) + i*eta.*sqtsqpsq;
            m12 = -i*sqrt(chihinv(k)/chih(k))*sqtsqpsq;
            m21 = i*sqrt(chih(k)/chihinv(k))*sqtsqpsq;
            m22 = cos(qtsq) - i*eta.*sqtsqpsq;

            % matrix product
            S0_new = m11.*S0 + m12.*SH;
            SH = m21.*S0 + m22.*SH;
            S0 = S0_new;
        end


        R1 = R1 + 0.5*abs(SH./S0).^2;

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
