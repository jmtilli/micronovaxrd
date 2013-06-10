% Convert a fixed mixture into a material. xyspace and zspace are the values
% calculated previously by mix_latticeconst. You must call mix_latticeconst
% first to calculate these! To create a strained material, they may be modified
% by the caller.
function material = mix_lattice(fixmix,xyspace,zspace)
  prop = [fixmix{:,1}];
  propsum = sum(prop);
  mixturesize = size(fixmix);
  nmixtures = mixturesize(1);

  % Maximum relative difference in unit cell sizes.
  % Theoretically, this should be 0, but due to floating
  % point inaccuracy, we have to use a finite number here.
  % If the scaled unit cell sizes of different materials
  % differ more than this, we know they have incompatible lattices.
  % However, the converse is not necessarily true, so you have to
  % make sure that you don't mix incompatible materials.
  tol = 1e-3;

  lambdatol = 1e-4;

  occupation = [];
  %V = [];
  V = zeros(nmixtures,1);
  lambda = zeros(nmixtures,1);
  H = zeros(nmixtures,3);

  B = [];
  hoenl = [];
  sf_a = [];
  sf_b = [];
  sf_c = [];

  r = [];
  %H = [];
  poissonratio = 0;

  for k = 1:nmixtures
      Bk = fixmix{k,2}.B;
      hoenl_k = fixmix{k,2}.hoenl;
      sf_a_k = fixmix{k,2}.sf_a;
      sf_b_k = fixmix{k,2}.sf_b;
      sf_c_k = fixmix{k,2}.sf_c;

      Vk = fixmix{k,2}.V;
      lambda(k) = fixmix{k,2}.lambda;
      Hk = fixmix{k,2}.H;
      rk = fixmix{k,2}.r;
      pk = fixmix{k,1};
      occupation_k = fixmix{k,2}.occupation;
      poissonratio = poissonratio + pk*fixmix{k,2}.poissonratio;

      % Calculate new unit cell volume and add it to the list
      xyspace_k = fixmix{k,2}.xyspace;
      zspace_k = fixmix{k,2}.zspace;
      Vk2 = Vk * xyspace^2 * zspace / xyspace_k^2 / zspace_k;
      %V = [V; Vk2];
      V(k) = Vk2;

      natoms = length(hoenl_k);
      %H = [H; Hk];
      H(k,:) = Hk;
      B = [B; Bk];
      hoenl = [hoenl; hoenl_k];
      sf_a = [sf_a; sf_a_k];
      sf_b = [sf_b; sf_b_k];
      sf_c = [sf_c; sf_c_k];
      r = [r; rk];
      occupation = [occupation; pk*occupation_k];
  end
  if not(all(abs(V./mean(V) - 1) < tol))
      error('Mixture of incompatible materials');
  end
  V = mean(V);
  if not(all(abs(lambda./mean(lambda) - 1) < lambdatol))
      error('Mixture of incompatible materials');
  end
  lambda = mean(lambda);
  if not(all(all(ones(nmixtures,1)*H(1,:) == H)))
      error('Mixture of materials with different orientations');
  end
  H = H(1,:);

  % lists
  material.V = V;
  material.lambda = lambda;
  material.r = r;
  material.B = B;
  material.hoenl = hoenl;
  material.sf_a = sf_a;
  material.sf_b = sf_b;
  material.sf_c = sf_c;
  material.occupation = occupation / propsum;
  material.poissonratio = poissonratio / propsum;

  % constants
  material.H = H;
  material.zspace = zspace;
  material.xyspace = xyspace;
  % We don't add Poisson's ratio to the lattice properties since strain is
  % already calculated.

  % Here we use a dirty trick: 1/Inf == 0
  %electricprop.chi0 = susc(occupation, r, Z, V, [0,0,0], Inf, lambda);
  %electricprop.chih = susc(occupation, r, Z, V, H, zspace, lambda);
  %electricprop.chihinv = susc(occupation, r, Z, V, -H, zspace, lambda);
end
