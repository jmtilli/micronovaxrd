% Calculate the elastic properties of a fixed mixture
%
% parameters:
% fixmix: a fixed mixture of elastic properties. It can also be (and usually
% is) a fixed mixture of materials, since the material data type is inherited
% from elastic properties.
% returns: a single elastic properties object. This will be a plain elastic
% properties object also in the case that fixmix is a mixture of materials.

function elasticprop = mix_latticeconst(fixmix)
  prop = [fixmix{:,1}];
  propsum = sum(prop);
  mixturesize = size(fixmix);
  nmixtures = mixturesize(1);
  poissonratio = 0;
  xyspace = 0;
  zspace = 0;
  % mixturesize(2) == 2
  for k = 1:nmixtures
      poissonratio = poissonratio + fixmix{k,1}*fixmix{k,2}.poissonratio;
      xyspace = xyspace + fixmix{k,1}*fixmix{k,2}.xyspace;
      zspace = zspace + fixmix{k,1}*fixmix{k,2}.zspace;
  end
  elasticprop.poissonratio = poissonratio/propsum;
  elasticprop.xyspace = xyspace/propsum;
  elasticprop.zspace = zspace/propsum;
end
