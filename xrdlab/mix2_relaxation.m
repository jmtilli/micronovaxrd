% Convert a variable mixture to a single material.
% If the material is strained, forcexyspace must be specified.
% Otherwise, it must be set to 0.
%
% XXX XXX XXX: do not arbitrarily modify this! matsusc2_premix
% relies on certain properties.
function material = mix2_relaxation(varmix, basexyspace, rel)
  % First we calculate xyspace, zspace and poissonratio
  % for the material in relaxed state. Then we calculate
  % new xyspace and zspace based on strain.
  x = varmix.x;

  % First sweep: calculate only lattice constants
  elasticprop1 = mix_latticeconst(varmix.first);
  elasticprop2 = mix_latticeconst(varmix.second);
  tempmixture = {1-x, elasticprop1; x, elasticprop2};
  elasticprop = mix_latticeconst(tempmixture);
  xyspace = elasticprop.xyspace;
  zspace = elasticprop.zspace;

  forcexyspace = rel*xyspace + (1-rel)*basexyspace;


  % Since xyspace is forced to have a specific value, we calculate
  % new zspace from poisson's ratio
  zspace = ((-2*elasticprop.poissonratio/(1-elasticprop.poissonratio) * (forcexyspace-xyspace)/xyspace) + 1) * zspace;
  xyspace = forcexyspace;

  % Second sweep: calculate all the other properties
  material1 = mix_lattice(varmix.first, xyspace, zspace);
  material2 = mix_lattice(varmix.second, xyspace, zspace);
  tempmixture = {1-x, material1; x, material2};
  material = mix_lattice(tempmixture, xyspace, zspace);

  % if the material is strained, do not include Poisson's ratio
  if rel ~= 1
      material.poissonratio = []; % already strained
  end
end
