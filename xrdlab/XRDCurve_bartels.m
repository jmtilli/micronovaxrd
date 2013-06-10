% reimplementation of XRDCurve to make sure it works correctly.

function R = XRDCurve_bartels(lambda, d, zspace, suscdata, theta, stddevrad, thetaoffset)
  
    if exist('thetaoffset','var')
        theta = theta - thetaoffset;
    end

    chi0 = suscdata(:,1);
    chih = suscdata(:,2);
    chihinv = suscdata(:,3);

    gamma0 = sin(theta);
    gammah = -sin(theta);
    % ... or gamma0 = -sin(theta) and gammah = sin(theta)
    b = gamma0./gammah;

    % XXX: should lambda be dispersion-corrected here?
    theta_B = asin(lambda./(2*zspace)); % Bragg's law. Order of reflection included in zpace.

    nlayers = length(d);
    ntheta = length(theta);

    R = zeros(size(theta));

    % XXX: is C layer-dependent?
    for C=[1,abs(cos(2*theta_B(end)))]
        %eta = foo;

        %XXX XXX XXX TODO
        %alphah = (sin(2*theta_B)*ones(1,ntheta)) * (ones(nlayers,1)*theta - theta_B*ones(1,ntheta));

        %------------------------------------------ the difference is here!
        % approximation
        alphah = -2*(sin(2*theta_B)*ones(1,ntheta)) .* (ones(nlayers,1)*theta - theta_B*ones(1,ntheta));
        % real
        alphah = -4*(ones(nlayers,1)*gamma0-sin(theta_B)*ones(1,ntheta)).* (sin(theta_B)*ones(1,ntheta));
        %-------------------------------------------

        eta = (alphah .* (ones(nlayers,1)*b) + chi0*(1-b)) ./ (2*C * (sqrt(chih.*chihinv) * sqrt(abs(b))));


        % Darwin-Prins formula
        etasubstr = eta(end,:);
        X = etasubstr - sign(real(etasubstr)) .* sqrt(etasubstr.^2 - 1);

        for k=(nlayers-1):(-1):1
            % XXX: should lambda be dispersion-corrected here?
            etak = eta(k,:);
            T = pi*C*sqrt(chih(k).*chihinv(k))*d(k) ./ (lambda*sqrt(abs(gamma0.*gammah)));
            sqrtterm = sqrt(etak.^2-1);
            expfactor = exp(-i*T.*sqrtterm);
            S1 = (X - etak + sqrtterm).*expfactor;
            S2 = (X - etak - sqrtterm)./expfactor;
            X = etak + sqrtterm.*(S1+S2)./(S1-S2);
        end
        R = R + 0.5*abs(X).^2;
    end




    % ---------------------------------

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
