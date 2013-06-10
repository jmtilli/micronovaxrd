function [newdeg,newmeas] = crop(deg,meas,first_angle,last_angle)
  newdeg=[];
  newmeas=[];
  for k=1:length(deg)
    if deg(k) >= first_angle && deg(k) <= last_angle
      newdeg=[newdeg, deg(k)];
      newmeas=[newmeas, meas(k)];
    end
  end
end
