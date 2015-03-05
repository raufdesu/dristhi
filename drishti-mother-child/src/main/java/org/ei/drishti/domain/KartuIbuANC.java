package org.ei.drishti.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.TypeDiscriminator;
import org.motechproject.model.MotechBaseDataObject;

@TypeDiscriminator("doc.type === 'kartu_ibu_ANC'")
public class KartuIbuANC extends MotechBaseDataObject {
	@JsonProperty
	private String motherId;
	@JsonProperty
	private String id;
	@JsonProperty
	private String ANC_date;
	@JsonProperty
	private String Jamkesmas;
	@JsonProperty
	private String CaraMasukTempatPelayanan;
	@JsonProperty
	private String UsiaKlinis;
	@JsonProperty
	private String TrimesterKe;
	@JsonProperty
	private String AnamnesisIbu;
	@JsonProperty
	private String BB;
	@JsonProperty
	private String TDSistolik;
	@JsonProperty
	private String TDDiastolik;
	@JsonProperty
	private String LILA;
	@JsonProperty
	private String StatusGiziibu;
	

	private KartuIbuANC()
	{
		
	}

	public KartuIbuANC(String motherId) {
		this.motherId = motherId;
	}

	public String getmotherId() {
		return motherId;
	}

	public void setmotherId(String motherId) {
		this.motherId = motherId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getANC_date() {
		return ANC_date;
	}
	public void setANC_date(String ANC_date) {
		this.ANC_date = ANC_date;
	}

	public String getPJamkesmas() {
		return Jamkesmas;
	}

	public void setJamkesmas(String Jamkesmas) {
		this.Jamkesmas = Jamkesmas;
	}

	public String getCaraMasukTempatPelayanan() {
		return CaraMasukTempatPelayanan;
	}

	public void setCaraMasukTempatPelayanan(String CaraMasukTempatPelayanan) {
		this.CaraMasukTempatPelayanan = CaraMasukTempatPelayanan;
	}

	public String getUsiaKlinis() {
		return UsiaKlinis;
	}

	public void setUsiaKlinis(String UsiaKlinis) {
		this.UsiaKlinis = UsiaKlinis;
	}

	public void setTrimesterKe(String TrimesterKe) {
		this.TrimesterKe = TrimesterKe;
	}

	public String getAnamnesisIbu() {
		return AnamnesisIbu;
	}

	public void setAnamnesisIbu(String AnamnesisIbu) {
		this.AnamnesisIbu = AnamnesisIbu;
	}

	public String getBB() {
		return BB;
	}

	public void setBB(String BB) {
		this.BB = BB;
	}

	public String getTDSistolik() {
		return TDSistolik;
	}

	public void setTDSistolik(String TDSistolik) {
		this.TDSistolik = TDSistolik;
	}

	public String getTDDiastolik() {
		return TDDiastolik;
	}

	public void setTDDiastolik(String TDDiastolik) {
		this.TDDiastolik = TDDiastolik;
	}

	public String getLILA() {
		return LILA;
	}

	public void setLILA(String LILA) {
		this.LILA = LILA;
	}

	public String getStatusGiziibu() {
		return StatusGiziibu;
	}

	public void setStatusGiziibu(String StatusGiziibu) {
		this.StatusGiziibu = StatusGiziibu;
	}

	

	@Override
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o, "id", "revision");
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "id", "revision");
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public KartuIbuANC withmotherId(String motherId) {
		setmotherId(motherId);
		return this;
	}

	public KartuIbuANC withANC_date(String ANC_date) {
		setANC_date(ANC_date);
		return this;
	}

	public KartuIbuANC withJamkesmas(String Jamkesmas) {
		setJamkesmas(Jamkesmas);
		return this;
	}

	public KartuIbuANC withUsiaKlinis(String UsiaKlinis) {
		setUsiaKlinis(UsiaKlinis);
		return this;
	}

	public KartuIbuANC withTrimesterKe(String TrimesterKe) {
		setTrimesterKe(TrimesterKe);
		return this;
	}

	
	public KartuIbuANC withid(String id) {
		setId(id);
		return this;
	}

	public KartuIbuANC withBB(String BB) {
		setId(BB);
        return this;
    }
}
