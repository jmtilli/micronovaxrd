% alpha_0, rho_e and beta_coeff must be row vectors
% r is the bottom interface roughness
function R = xrrCurve_s(alpha_0, d, chi_0, r, lambda, stddevrad)
  r_e = 2.817940325e-15; % Classical electron radius
  kz = 2*pi/lambda * sqrt(chi_0*ones(size(alpha_0)) + ones(size(chi_0))*sin(alpha_0).^2);

  R_Fresnel = zeros(length(chi_0)-1, length(alpha_0));
  R_total = zeros(1, length(alpha_0));

  % Calculate Fresnel reflectivity coefficients
  for k=1:(length(chi_0)-1)
    denom = kz(k,:) + kz(k+1,:);
    R_Fresnel(k,:) = (kz(k,:) - kz(k+1,:)) ./ (denom + (denom==0));
    R_Fresnel(k,:) = R_Fresnel(k,:).*exp(-2*kz(k,:).*kz(k+1,:)*r(k)^2);
  end

  % Calculate total reflectivity
  for k=(length(chi_0)-1):(-1):1
    expfactor = exp(-2*i*kz(k+1,:)*d(k+1));
    R_total = (R_Fresnel(k,:) + R_total.*expfactor)./(1+R_total.*R_Fresnel(k,:).*expfactor);
  end
  R = abs(R_total).^2;

  % Instrument convolution
  stddevs = 4;
  dalpha0rad = (alpha_0(end)-alpha_0(1))/(length(alpha_0)-1);
  gfilt = gaussian_filter(dalpha0rad, stddevrad, stddevs);
  if(length(gfilt) > 1)
  %    if(!all(abs(alpha_0-linspace(alpha_0(1),alpha_0(end),length(alpha_0))) <   linthreshold*dalpha0rad))
  %        error("alpha_0 not uniformly spaced");
  %    end
      R = apply_odd_filter(gfilt, R);
  end
end
