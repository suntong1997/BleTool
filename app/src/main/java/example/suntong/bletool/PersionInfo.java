package example.suntong.bletool;

public class PersionInfo {
    float height;
    float weight;
    float strideLength;
    float gender;
    float runStrideLength;

    public PersionInfo(float height, float weight, float strideLength, float gender, float runStrideLength) {
        this.height = height;
        this.weight = weight;
        this.strideLength = strideLength;
        this.gender = gender;
        this.runStrideLength = runStrideLength;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getStrideLength() {
        return strideLength;
    }

    public void setStrideLength(float strideLength) {
        this.strideLength = strideLength;
    }

    public float getGender() {
        return gender;
    }

    public void setGender(float gender) {
        this.gender = gender;
    }

    public float getRunStrideLength() {
        return runStrideLength;
    }

    public void setRunStrideLength(float runStrideLength) {
        this.runStrideLength = runStrideLength;
    }
}
